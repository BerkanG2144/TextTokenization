package commands;

import core.AnalysisResult;
import core.Match;
import core.Token;
import exceptions.CommandException;
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
        if (args.length < 2 || args.length > 4) {
            throw new CommandException("inspect command requires: inspect <id> <id> [context] [minLen]");
        }

        String id1 = args[0];
        String id2 = args[1];
        int contextSize = 0;
        int displayMinLenArg = -1;

        if (args.length >= 3) {
            try {
                contextSize = Integer.parseInt(args[2]);
                if (contextSize < 0) {
                    throw new CommandException("Context size must be non-negative");
                }
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid number format for context size");
            }
        }

        if (args.length == 4) {
            try {
                displayMinLenArg = Integer.parseInt(args[3]);
                if (displayMinLenArg <= 0) {
                    throw new CommandException("minLen must be positive");
                }
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid number format for minLen");
            }
        }

        // Get analysis results
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }

        // Find the match result for these two texts (in given order)
        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new CommandException("No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        if (result.getMatches().isEmpty()) {
            throw new CommandException("No matches found for the specified text pair");
        }

        // Enter inspect mode
        enterInspectMode(result, id1, id2, contextSize, displayMinLenArg);

        return "OK, exit inspection mode";
    }

    /**
     * Enters the interactive inspect mode.
     */
    private void enterInspectMode(MatchResult result, String id1, String id2, int contextSize, int displayMinLenArg) {
        int displayMinLen = (displayMinLenArg > 0) ? displayMinLenArg : 1;
        List<Match> originalMatches = result.getMatches();
        List<Match> sortedMatches = new ArrayList<>();
        boolean needSwap = !result.getText1().identifier().equals(id1);
        for (Match match : originalMatches) {
            if (match.length() >= displayMinLen) {
                sortedMatches.add(match);
            }
        }
        if (sortedMatches.isEmpty()) {
            System.out.println("No matches found with minimum length " + displayMinLen);
            return;
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

        String pairKey = (id1.compareTo(id2) <= 0) ? (id1 + "-" + id2) : (id2 + "-" + id1);
        Set<Match> treatedMatches = treatedMatchesPerPair.computeIfAbsent(pairKey, k -> new HashSet<>());

        Map<Match, String> decisions = new HashMap<>();
        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        int currentIndex = -1;
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (!treatedMatches.contains(sortedMatches.get(i))) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            updateAnalysisResult(result, modifiedMatches, id1, id2);
            return;
        }
        while (true) {
            Match currentMatch = sortedMatches.get(currentIndex);
            displayMatch(currentMatch, result, id1, id2, contextSize, decisions);

            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                input = "C";
            }

            switch (input) {
                case "C": {
                    int nextIndex = findNextUntreatedWrap(currentIndex, sortedMatches, treatedMatches);
                    if (nextIndex == -1) { // global none left
                        updateAnalysisResult(result, modifiedMatches, id1, id2);
                        return;
                    }
                    currentIndex = nextIndex;
                    break;
                }
                case "P": {
                    int prevIndex = findPrevUntreatedWrap(currentIndex, sortedMatches, treatedMatches);
                    if (prevIndex == -1) {
                        System.out.println("No previous untreated matches.");
                    } else {
                        currentIndex = prevIndex;
                    }
                    break;
                }
                case "A":
                case "I":
                case "X": {
                    String decision = input.equals("A") ? "Accept" : input.equals("I") ? "Ignore" : "Exclude";
                    decisions.put(currentMatch, decision);
                    treatedMatches.add(currentMatch);

                    if (!input.equals("A")) {
                        modifiedMatches.removeIf(m -> m.equals(currentMatch));
                    }

                    int nextIndex = findNextUntreatedWrap(currentIndex, sortedMatches, treatedMatches);
                    if (nextIndex == -1) {
                        updateAnalysisResult(result, modifiedMatches, id1, id2);
                        return;
                    }
                    currentIndex = nextIndex;
                    break;
                }
                case "B":
                    updateAnalysisResult(result, modifiedMatches, id1, id2);
                    return;
                default:
                    System.out.println("Invalid command. Use C, P, A, I, X, or B.");
            }
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
    private void displayMatch(Match match, MatchResult result, String id1, String id2,
                              int contextSize, Map<Match, String> decisions) {
        boolean needSwap = !result.getText1().identifier().equals(id1);

        String firstText = needSwap ? result.getText2().content() : result.getText1().content();
        String secondText = needSwap ? result.getText1().content() : result.getText2().content();
        List<Token> firstSeq = needSwap ? result.getSequence2() : result.getSequence1();
        List<Token> secondSeq = needSwap ? result.getSequence1() : result.getSequence2();

        int firstPos = needSwap ? match.startPosSequence2() : match.startPosSequence1();
        int secondPos = needSwap ? match.startPosSequence1() : match.startPosSequence2();

        if (firstPos < firstSeq.size() && secondPos < secondSeq.size()) {
            int startChar1 = firstSeq.get(firstPos).getStartPosition();
            int lastTokenIndex1 = Math.min(firstPos + match.length() - 1, firstSeq.size() - 1);
            int endChar1 = firstSeq.get(lastTokenIndex1).getEndPosition();

            int startChar2 = secondSeq.get(secondPos).getStartPosition();
            int lastTokenIndex2 = Math.min(secondPos + match.length() - 1, secondSeq.size() - 1);
            int endChar2 = secondSeq.get(lastTokenIndex2).getEndPosition();

            String context1 = extractContext(firstText, startChar1, endChar1, contextSize);
            String context2 = extractContext(secondText, startChar2, endChar2, contextSize);

            System.out.println(context1);

            String matchedPart1 = firstText.substring(startChar1, endChar1);
            String matchedPart2 = secondText.substring(startChar2, endChar2);

            int contextStart1 = Math.max(0, startChar1 - contextSize);
            int contextStart2 = Math.max(0, startChar2 - contextSize);

            int prefixLength1 = (contextStart1 > 0) ? 3 : 0;
            int prefixLength2 = (contextStart2 > 0) ? 3 : 0;

            int matchStartInContext1 = prefixLength1 + (startChar1 - contextStart1);
            int matchStartInContext2 = prefixLength2 + (startChar2 - contextStart2);

            StringBuilder underline1 = new StringBuilder();
            for (int i = 0; i < matchStartInContext1; i++) {
                underline1.append(" ");
            }
            underline1.append("^".repeat(matchedPart1.length()));
            System.out.println(underline1.toString());

            System.out.println(context2);

            StringBuilder underline2 = new StringBuilder();
            for (int i = 0; i < matchStartInContext2; i++) {
                underline2.append(" ");
            }
            underline2.append("^".repeat(matchedPart2.length()));
            System.out.println(underline2.toString());
        }
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
        // Create a new MatchResult with the modified matches
        MatchResult newResult = new MatchResult(
                originalResult.getText1(), originalResult.getText2(),
                originalResult.getSequence1(), originalResult.getSequence2(),
                modifiedMatches, originalResult.getTokenizationStrategy(), originalResult.getMinMatchLength()
        );

        // Update the analysis result in the AnalyzeCommand
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
}
