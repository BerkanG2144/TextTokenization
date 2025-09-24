package commands;

import core.AnalysisResult;
import core.Match;
import core.Token;
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
 * Usage: inspect <id> <id> [context] [minLen]
 *
 * @author [Dein u-Kürzel]
 */
public class InspectCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;
    // Store treated matches persistently across inspect calls
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
     * Clears treated matches when a new analysis is run
     */
    public void clearTreatedMatches() {
        treatedMatchesPerPair.clear();
    }

    /* ---------- helpers ---------- */

    private boolean isDisplayable(Match m, int minLen) {
        return m.getLength() >= minLen;
    }

    /**
     * Enters the interactive inspect mode.
     */
    /**
     * Enters the interactive inspect mode.
     */
    /**
     * Enters the interactive inspect mode.
     */
    private void enterInspectMode(MatchResult result, String id1, String id2, int contextSize, int displayMinLenArg) {
        // Determine the minimum length for display
        int displayMinLen = (displayMinLenArg > 0) ? displayMinLenArg : 1; // Default to 1 if not specified

        List<Match> originalMatches = result.getMatches();

        // Sort matches by position in "first text of the command order", tie-breaker: longer first
        List<Match> sortedMatches = new ArrayList<>();
        boolean needSwap = !result.getText1().getIdentifier().equals(id1);

        // Filter matches by display minimum length
        for (Match match : originalMatches) {
            if (match.getLength() >= displayMinLen) {
                sortedMatches.add(match);
            }
        }

        if (sortedMatches.isEmpty()) {
            System.out.println("No matches found with minimum length " + displayMinLen);
            return;
        }

        // Sort by position in first text (as specified in command), then by length descending
        if (needSwap) {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.getStartPosSequence2(), m2.getStartPosSequence2());
                return (c != 0) ? c : -Integer.compare(m1.getLength(), m2.getLength());
            });
        } else {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.getStartPosSequence1(), m2.getStartPosSequence1());
                return (c != 0) ? c : -Integer.compare(m1.getLength(), m2.getLength());
            });
        }

        // Get or create the treated matches set for this text pair (order-independent key)
        String pairKey = (id1.compareTo(id2) <= 0) ? (id1 + "-" + id2) : (id2 + "-" + id1);
        Set<Match> treatedMatches = treatedMatchesPerPair.computeIfAbsent(pairKey, k -> new HashSet<>());

        // Track decisions for display purposes
        Map<Match, String> decisions = new HashMap<>();
        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        // Find first untreated match
        int currentIndex = -1;
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (!treatedMatches.contains(sortedMatches.get(i))) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            System.out.println("All matches have been treated. Inspection complete.");
            updateAnalysisResult(result, modifiedMatches, id1, id2);
            return;
        }

        while (true) {
            Match currentMatch = sortedMatches.get(currentIndex);
            displayMatch(currentMatch, result, id1, id2, contextSize, decisions);

            // Get user input
            System.out.print("> ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) input = "C";

            switch (input) {
                case "C": {
                    // Find next untreated match
                    int nextIndex = findNextUntreatedSimple(currentIndex, sortedMatches, treatedMatches);
                    if (nextIndex == -1) {
                        System.out.println("No more untreated matches. Inspection done.");
                        updateAnalysisResult(result, modifiedMatches, id1, id2);
                        return;
                    }
                    currentIndex = nextIndex;
                    break;
                }
                case "P": {
                    // Find previous untreated match
                    int prevIndex = findPrevUntreatedSimple(currentIndex, sortedMatches, treatedMatches);
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
                    // Mark as treated and update decision
                    String decision = input.equals("A") ? "Accept" : input.equals("I") ? "Ignore" : "Exclude";
                    decisions.put(currentMatch, decision);
                    treatedMatches.add(currentMatch);

                    // Remove from modified matches if not accepted
                    if (!input.equals("A")) {
                        modifiedMatches.removeIf(m -> m.equals(currentMatch));
                    }

                    // Find next untreated match
                    int nextIndex = findNextUntreatedSimple(currentIndex, sortedMatches, treatedMatches);
                    if (nextIndex == -1) {
                        System.out.println("No more untreated matches. Inspection done.");
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

    /**
     * Simple helper to find next untreated match (no wraparound).
     */
    private int findNextUntreatedSimple(int currentIndex, List<Match> matches, Set<Match> treated) {
        for (int i = currentIndex + 1; i < matches.size(); i++) {
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Simple helper to find previous untreated match.
     */
    private int findPrevUntreatedSimple(int currentIndex, List<Match> matches, Set<Match> treated) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Displays the current match with context.
     */
    /**
     * Displays the current match with context.
     */
    private void displayMatch(Match match, MatchResult result, String id1, String id2,
                              int contextSize, Map<Match, String> decisions) {
        boolean needSwap = !result.getText1().getIdentifier().equals(id1);

        // Get the texts and sequences in command order
        String firstText = needSwap ? result.getText2().getContent() : result.getText1().getContent();
        String secondText = needSwap ? result.getText1().getContent() : result.getText2().getContent();
        List<Token> firstSeq = needSwap ? result.getSequence2() : result.getSequence1();
        List<Token> secondSeq = needSwap ? result.getSequence1() : result.getSequence2();

        // Get match positions in command order
        int firstPos = needSwap ? match.getStartPosSequence2() : match.getStartPosSequence1();
        int secondPos = needSwap ? match.getStartPosSequence1() : match.getStartPosSequence2();

        if (firstPos < firstSeq.size() && secondPos < secondSeq.size()) {
            // Calculate character positions correctly
            int startChar1 = firstSeq.get(firstPos).getStartPosition();
            int lastTokenIndex1 = Math.min(firstPos + match.getLength() - 1, firstSeq.size() - 1);
            int endChar1 = firstSeq.get(lastTokenIndex1).getEndPosition();

            int startChar2 = secondSeq.get(secondPos).getStartPosition();
            int lastTokenIndex2 = Math.min(secondPos + match.getLength() - 1, secondSeq.size() - 1);
            int endChar2 = secondSeq.get(lastTokenIndex2).getEndPosition();

            // Extract and display context
            String context1 = extractContext(firstText, startChar1, endChar1, contextSize);
            String context2 = extractContext(secondText, startChar2, endChar2, contextSize);

            System.out.println(context1);

            // Calculate the position and length of the matched part within the context
            String matchedPart1 = firstText.substring(startChar1, endChar1);
            String matchedPart2 = secondText.substring(startChar2, endChar2);

            // Find the matched part in the context (accounting for "..." prefix)
            int contextStart1 = Math.max(0, startChar1 - contextSize);
            int contextStart2 = Math.max(0, startChar2 - contextSize);

            int prefixLength1 = (contextStart1 > 0) ? 3 : 0; // Length of "..."
            int prefixLength2 = (contextStart2 > 0) ? 3 : 0;

            int matchStartInContext1 = prefixLength1 + (startChar1 - contextStart1);
            int matchStartInContext2 = prefixLength2 + (startChar2 - contextStart2);

            // Print underline for first match
            StringBuilder underline1 = new StringBuilder();
            for (int i = 0; i < matchStartInContext1; i++) {
                underline1.append(" ");
            }
            underline1.append("^".repeat(matchedPart1.length()));
            System.out.println(underline1.toString());

            System.out.println(context2);

            // Print underline for second match
            StringBuilder underline2 = new StringBuilder();
            for (int i = 0; i < matchStartInContext2; i++) {
                underline2.append(" ");
            }
            underline2.append("^".repeat(matchedPart2.length()));
            System.out.println(underline2.toString());
        }

        // Display current decision
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
