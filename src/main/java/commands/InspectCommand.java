package commands;

import core.AnalysisResult;
import core.Match;
import core.Token;
import exceptions.CommandException;
import exceptions.AnalysisNotPerformedException;
import exceptions.TextNotFoundException;
import exceptions.InsufficientDataException;
import matching.MatchResult;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

/**
 * Command to enter interactive inspect mode for a text pair comparison.
 *
 * @author ujnaa
 */
public class InspectCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;
    private final Map<String, Set<Match>> treatedMatchesPerPair = new HashMap<>();

    /**
     * Creates a new InspectCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     * @param scanner scanner for user input
     */
    public InspectCommand(AnalyzeCommand analyzeCommand, Scanner scanner) {
        this.analyzeCommand = analyzeCommand;
        this.scanner = scanner;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        InspectParameters params = parseArguments(args);
        MatchResult result = getMatchResult(params.id1, params.id2);

        if (result.getMatches().isEmpty()) {
            throw new InsufficientDataException(1, 0, "matches");
        }

        enterInspectMode(result, params);
        return "OK, exit inspection mode";
    }

    /**
     * Parses command arguments and validates them.
     */
    private InspectParameters parseArguments(String[] args) throws CommandException {
        if (args.length < 2 || args.length > 4) {
            throw new CommandException("inspect command requires: inspect <id> <id> [context] [minLen]");
        }

        String id1 = args[0];
        String id2 = args[1];
        int contextSize = parseContextSize(args);
        int displayMinLen = parseDisplayMinLen(args);

        return new InspectParameters(id1, id2, contextSize, displayMinLen);
    }

    /**
     * Parses context size from arguments.
     */
    private int parseContextSize(String[] args) throws CommandException {
        if (args.length < 3) {
            return 0;
        }

        try {
            int contextSize = Integer.parseInt(args[2]);
            if (contextSize < 0) {
                throw new CommandException("Context size must be non-negative");
            }
            return contextSize;
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for context size");
        }
    }

    /**
     * Parses display minimum length from arguments.
     */
    private int parseDisplayMinLen(String[] args) throws CommandException {
        if (args.length < 4) {
            return 1;
        }

        try {
            int displayMinLen = Integer.parseInt(args[3]);
            if (displayMinLen <= 0) {
                throw new CommandException("minLen must be positive");
            }
            return displayMinLen;
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for minLen");
        }
    }

    /**
     * Gets the match result for the specified text pair.
     */
    private MatchResult getMatchResult(String id1, String id2) throws CommandException {
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new AnalysisNotPerformedException();
        }

        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new TextNotFoundException(id1 + "/" + id2,
                    "No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        return result;
    }

    /**
     * Enters the interactive inspect mode.
     */
    private void enterInspectMode(MatchResult result, InspectParameters params) {
        InspectState state = initializeInspectState(result, params);

        if (state.sortedMatches.isEmpty()) {
            System.out.println("No matches found with minimum length " + params.displayMinLen);
            return;
        }

        int currentIndex = findFirstUntreatedMatch(state.sortedMatches, state.treatedMatches);
        if (currentIndex == -1) {
            updateAnalysisResult(result, state.modifiedMatches, params.id1, params.id2);
            return;
        }

        runInspectionLoop(result, params, state, currentIndex);
    }

    /**
     * Initializes the inspection state.
     */
    private InspectState initializeInspectState(MatchResult result, InspectParameters params) {
        List<Match> originalMatches = result.getMatches();
        List<Match> sortedMatches = filterAndSortMatches(originalMatches, result, params);

        String pairKey = createPairKey(params.id1, params.id2);
        Set<Match> treatedMatches = treatedMatchesPerPair.computeIfAbsent(pairKey, k -> new HashSet<>());

        Map<Match, String> decisions = new HashMap<>();
        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        return new InspectState(sortedMatches, treatedMatches, decisions, modifiedMatches);
    }

    /**
     * Filters matches by minimum length and sorts them.
     */
    private List<Match> filterAndSortMatches(List<Match> originalMatches, MatchResult result, InspectParameters params) {
        List<Match> sortedMatches = new ArrayList<>();
        boolean needSwap = !result.getText1().identifier().equals(params.id1);

        for (Match match : originalMatches) {
            if (match.length() >= params.displayMinLen) {
                sortedMatches.add(match);
            }
        }

        if (needSwap) {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.startPosSequence2(), m2.startPosSequence2());
                return (c != 0) ? c : -Integer.compare(m1.length(), m2.length());
            });
        } else {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.startPosSequence1(), m2.startPosSequence1());
                return (c != 0) ? c : -Integer.compare(m1.length(), m2.length());
            });
        }

        return sortedMatches;
    }

    /**
     * Creates a consistent pair key for caching treated matches.
     */
    private String createPairKey(String id1, String id2) {
        return (id1.compareTo(id2) <= 0) ? (id1 + "-" + id2) : (id2 + "-" + id1);
    }

    /**
     * Finds the first untreated match in the sorted list.
     */
    private int findFirstUntreatedMatch(List<Match> sortedMatches, Set<Match> treatedMatches) {
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (!treatedMatches.contains(sortedMatches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Runs the main inspection loop.
     */
    private void runInspectionLoop(MatchResult result, InspectParameters params, InspectState state, int startIndex) {
        int currentIndex = startIndex;

        while (true) {
            Match currentMatch = state.sortedMatches.get(currentIndex);
            displayMatch(currentMatch, result, params, state.decisions);

            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                input = "C";
            }

            InspectionAction action = handleUserInput(input, currentMatch, state, result, params);

            if (action.shouldExit) {
                updateAnalysisResult(result, state.modifiedMatches, params.id1, params.id2);
                return;
            }

            if (action.newIndex != -1) {
                currentIndex = action.newIndex;
            }
        }
    }

    /**
     * Handles user input and returns the action to take.
     */
    private InspectionAction handleUserInput(String input, Match currentMatch, InspectState state,
                                             MatchResult result, InspectParameters params) {
        switch (input) {
            case "C":
                return handleContinueCommand(state);
            case "P":
                return handlePreviousCommand(state);
            case "A":
            case "I":
            case "X":
                return handleDecisionCommand(input, currentMatch, state);
            case "B":
                return InspectionAction.exit();
            default:
                System.out.println("Invalid command. Use C, P, A, I, X, or B.");
                return InspectionAction.stay();
        }
    }

    /**
     * Handles the continue command.
     */
    private InspectionAction handleContinueCommand(InspectState state) {
        int nextIndex = findNextUntreatedWrap(-1, state.sortedMatches, state.treatedMatches);
        if (nextIndex == -1) {
            return InspectionAction.exit();
        }
        return InspectionAction.moveTo(nextIndex);
    }

    /**
     * Handles the previous command.
     */
    private InspectionAction handlePreviousCommand(InspectState state) {
        int prevIndex = findPrevUntreatedWrap(-1, state.sortedMatches, state.treatedMatches);
        if (prevIndex == -1) {
            System.out.println("No previous untreated matches.");
            return InspectionAction.stay();
        }
        return InspectionAction.moveTo(prevIndex);
    }

    /**
     * Handles decision commands (Accept, Ignore, eXclude).
     */
    private InspectionAction handleDecisionCommand(String input, Match currentMatch, InspectState state) {
        String decision = getDecisionText(input);
        state.decisions.put(currentMatch, decision);
        state.treatedMatches.add(currentMatch);

        if (!"A".equals(input)) {
            state.modifiedMatches.removeIf(m -> m.equals(currentMatch));
        }

        int nextIndex = findNextUntreatedWrap(-1, state.sortedMatches, state.treatedMatches);
        if (nextIndex == -1) {
            return InspectionAction.exit();
        }
        return InspectionAction.moveTo(nextIndex);
    }

    /**
     * Converts input to decision text.
     */
    private String getDecisionText(String input) {
        switch (input) {
            case "A": return "Accept";
            case "I": return "Ignore";
            case "X": return "Exclude";
            default: return "Unknown";
        }
    }

    private int findNextUntreatedWrap(int currentIndex, List<Match> matches, Set<Match> treated) {
        int n = matches.size();
        for (int step = 1; step <= n; step++) {
            int i = (currentIndex + step) % n;
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findPrevUntreatedWrap(int currentIndex, List<Match> matches, Set<Match> treated) {
        int n = matches.size();
        for (int step = 1; step <= n; step++) {
            int i = (currentIndex - step + n) % n;
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Displays the current match with context.
     */
    private void displayMatch(Match match, MatchResult result, InspectParameters params, Map<Match, String> decisions) {
        boolean needSwap = !result.getText1().identifier().equals(params.id1);

        String firstText = needSwap ? result.getText2().content() : result.getText1().content();
        String secondText = needSwap ? result.getText1().content() : result.getText2().content();
        List<Token> firstSeq = needSwap ? result.getSequence2() : result.getSequence1();
        List<Token> secondSeq = needSwap ? result.getSequence1() : result.getSequence2();

        int firstPos = needSwap ? match.startPosSequence2() : match.startPosSequence1();
        int secondPos = needSwap ? match.startPosSequence1() : match.startPosSequence2();

        displayMatchContext(match, firstText, secondText, firstSeq, secondSeq, firstPos, secondPos, params.contextSize);
        displayMatchDecision(match, decisions);
    }

    /**
     * Displays the match context with underlining.
     */
    private void displayMatchContext(Match match, String firstText, String secondText,
                                     List<Token> firstSeq, List<Token> secondSeq,
                                     int firstPos, int secondPos, int contextSize) {
        if (firstPos >= firstSeq.size() || secondPos >= secondSeq.size()) {
            return;
        }

        int startChar1 = firstSeq.get(firstPos).getStartPosition();
        int lastTokenIndex1 = Math.min(firstPos + match.length() - 1, firstSeq.size() - 1);
        int endChar1 = firstSeq.get(lastTokenIndex1).getEndPosition();

        int startChar2 = secondSeq.get(secondPos).getStartPosition();
        int lastTokenIndex2 = Math.min(secondPos + match.length() - 1, secondSeq.size() - 1);
        int endChar2 = secondSeq.get(lastTokenIndex2).getEndPosition();

        String context1 = extractContext(firstText, startChar1, endChar1, contextSize);
        String context2 = extractContext(secondText, startChar2, endChar2, contextSize);

        System.out.println(context1);
        printUnderline(firstText, startChar1, endChar1, contextSize);
        System.out.println(context2);
        printUnderline(secondText, startChar2, endChar2, contextSize);
    }

    /**
     * Prints underline for matched text.
     */
    private void printUnderline(String text, int startChar, int endChar, int contextSize) {
        int contextStart = Math.max(0, startChar - contextSize);
        int prefixLength = (contextStart > 0) ? 3 : 0;
        int matchStartInContext = prefixLength + (startChar - contextStart);

        String matchedPart = text.substring(startChar, endChar);
        StringBuilder underline = new StringBuilder();

        for (int i = 0; i < matchStartInContext; i++) {
            underline.append(" ");
        }
        underline.append("^".repeat(matchedPart.length()));
        System.out.println(underline.toString());
    }

    /**
     * Displays the current decision for the match.
     */
    private void displayMatchDecision(Match match, Map<Match, String> decisions) {
        String currentDecision = decisions.getOrDefault(match, "None");
        System.out.println("Current decision: " + currentDecision);
        System.out.println();
        System.out.println("[C]ontinue, [P]revious, [A]ccept, [I]gnore, e[X]clude, [B]ack? (C)");
    }

    /**
     * Extracts context around a match.
     */
    private String extractContext(String text, int start, int end, int contextSize) {
        int contextStart = Math.max(0, start - contextSize);
        int contextEnd = Math.min(text.length(), end + contextSize);

        StringBuilder result = new StringBuilder();

        if (contextStart > 0) {
            result.append("...");
        }

        result.append(text.substring(contextStart, contextEnd));

        if (contextEnd < text.length()) {
            result.append("...");
        }

        return result.toString();
    }

    /**
     * Updates the analysis result with modified matches.
     */
    private void updateAnalysisResult(MatchResult originalResult, List<Match> modifiedMatches, String id1, String id2) {
        MatchResult newResult = new MatchResult(
                originalResult.getText1(), originalResult.getText2(),
                originalResult.getSequence1(), originalResult.getSequence2(),
                modifiedMatches, originalResult.getTokenizationStrategy(), originalResult.getMinMatchLength()
        );
        analyzeCommand.updateMatchResult(id1, id2, newResult);
    }

    @Override
    public String getName() {
        return "inspect";
    }

    @Override
    public String getUsage() {
        return "inspect <id> <id> [context] [minLen] - Enter interactive inspect mode for a text pair";
    }

    /**
     * Parameters for the inspect command.
     */
    private static class InspectParameters {
        final String id1;
        final String id2;
        final int contextSize;
        final int displayMinLen;

        InspectParameters(String id1, String id2, int contextSize, int displayMinLen) {
            this.id1 = id1;
            this.id2 = id2;
            this.contextSize = contextSize;
            this.displayMinLen = displayMinLen;
        }
    }

    /**
     * State for the inspection process.
     */
    private static class InspectState {
        final List<Match> sortedMatches;
        final Set<Match> treatedMatches;
        final Map<Match, String> decisions;
        final List<Match> modifiedMatches;

        InspectState(List<Match> sortedMatches, Set<Match> treatedMatches,
                     Map<Match, String> decisions, List<Match> modifiedMatches) {
            this.sortedMatches = sortedMatches;
            this.treatedMatches = treatedMatches;
            this.decisions = decisions;
            this.modifiedMatches = modifiedMatches;
        }
    }

    /**
     * Action result from handling user input.
     */
    private static class InspectionAction {
        final boolean shouldExit;
        final int newIndex;

        private InspectionAction(boolean shouldExit, int newIndex) {
            this.shouldExit = shouldExit;
            this.newIndex = newIndex;
        }

        static InspectionAction exit() {
            return new InspectionAction(true, -1);
        }

        static InspectionAction moveTo(int index) {
            return new InspectionAction(false, index);
        }

        static InspectionAction stay() {
            return new InspectionAction(false, -1);
        }
    }
}