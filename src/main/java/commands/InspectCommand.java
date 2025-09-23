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

/**
 * Command to enter interactive inspect mode for a text pair comparison.
 * Usage: inspect <id> <id> [number]
 *
 * @author [Dein u-Kürzel]
 */
public class InspectCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;

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
        if (args.length < 2 || args.length > 3) {
            throw new CommandException("inspect command requires two or three arguments: inspect <id> <id> [number]");
        }

        String id1 = args[0];
        String id2 = args[1];
        int contextSize = 0;

        if (args.length == 3) {
            try {
                contextSize = Integer.parseInt(args[2]);
                if (contextSize < 0) {
                    throw new CommandException("Context size must be non-negative");
                }
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid number format for context size");
            }
        }

        // Get analysis results
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }

        // Find the match result for these two texts
        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new CommandException("No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        if (result.getMatches().isEmpty()) {
            throw new CommandException("No matches found for the specified text pair");
        }

        // Enter inspect mode
        enterInspectMode(result, id1, id2, contextSize);

        return "OK, exit inspection mode";
    }

    /**
     * Enters the interactive inspect mode.
     */
    private void enterInspectMode(MatchResult result, String id1, String id2, int contextSize) {
        List<Match> originalMatches = result.getMatches();

        // Sort matches by position in first text (command order)
        List<Match> sortedMatches = new ArrayList<>(originalMatches);
        boolean needSwap = !result.getText1().getIdentifier().equals(id1);

        if (needSwap) {
            // Sort by position in sequence2 (which corresponds to first text in command)
            sortedMatches.sort((m1, m2) -> Integer.compare(m1.getStartPosSequence2(), m2.getStartPosSequence2()));
        } else {
            // Sort by position in sequence1 (which corresponds to first text in command)
            sortedMatches.sort((m1, m2) -> Integer.compare(m1.getStartPosSequence1(), m2.getStartPosSequence1()));
        }

        // Track decisions for each match
        Map<Match, String> decisions = new HashMap<>();
        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        int currentIndex = 0;

        while (currentIndex < sortedMatches.size()) {
            Match currentMatch = sortedMatches.get(currentIndex);

            // Display current match
            displayMatch(currentMatch, result, id1, id2, contextSize, decisions);

            // Get user input
            System.out.print("> ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.isEmpty()) {
                input = "C"; // Default to Continue
            }

            switch (input) {
                case "C": // Continue
                    currentIndex++;
                    break;
                case "P": // Previous
                    currentIndex = Math.max(0, currentIndex - 1);
                    break;
                case "A": // Accept
                    decisions.put(currentMatch, "Accept");
                    currentIndex++;
                    break;
                case "I": // Ignore
                    decisions.put(currentMatch, "Ignore");
                    // Remove from modified matches
                    modifiedMatches.removeIf(m -> m.equals(currentMatch));
                    currentIndex++;
                    break;
                case "X": // Exclude
                    decisions.put(currentMatch, "Exclude");
                    // Remove from modified matches
                    modifiedMatches.removeIf(m -> m.equals(currentMatch));
                    currentIndex++;
                    break;
                case "B": // Back (exit)
                    updateAnalysisResult(result, modifiedMatches, id1, id2);
                    return;
                default:
                    System.out.println("Invalid command. Use C, P, A, I, X, or B.");
                    continue;
            }
        }

        // All matches inspected
        System.out.println("Inspection complete. Exit inspection mode");
        updateAnalysisResult(result, modifiedMatches, id1, id2);
    }

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
            // Calculate character positions
            int startChar1 = firstSeq.get(firstPos).getStartPosition();
            int endChar1 = firstSeq.get(Math.min(firstPos + match.getLength() - 1, firstSeq.size() - 1)).getEndPosition();

            int startChar2 = secondSeq.get(secondPos).getStartPosition();
            int endChar2 = secondSeq.get(Math.min(secondPos + match.getLength() - 1, secondSeq.size() - 1)).getEndPosition();

            // Extract and display context
            String context1 = extractContext(firstText, startChar1, endChar1, contextSize);
            String context2 = extractContext(secondText, startChar2, endChar2, contextSize);

            System.out.println(context1);
            System.out.println("^".repeat(Math.max(1, endChar1 - startChar1)));
            System.out.println(context2);
            System.out.println("^".repeat(Math.max(1, endChar2 - startChar2)));
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
        return "inspect <id> <id> [number] - Enter interactive inspect mode for a text pair";
    }
}