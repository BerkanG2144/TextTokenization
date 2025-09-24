package commands;

import core.AnalysisResult;
import core.Match;
import exceptions.CommandException;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Command to enter interactive edit mode for a text pair comparison.
 *
 * @author ujnaa
 */
public class EditCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;
    /**
     * Creates a new EditCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     * @param scanner scanner for user input
     */
    public EditCommand(AnalyzeCommand analyzeCommand, Scanner scanner) {
        this.analyzeCommand = analyzeCommand;
        this.scanner = scanner;
    }
    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("edit command requires exactly two arguments: edit <id> <id>");
        }
        String id1 = args[0];
        String id2 = args[1];
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }
        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new CommandException("No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }
        enterEditMode(result, id1, id2);
        return "OK, exit editing mode.";
    }

    /**
     * Enters the interactive edit mode.
     *
     * @param result the match result to edit
     * @param id1 first text identifier from command
     * @param id2 second text identifier from command
     */
    private void enterEditMode(MatchResult result, String id1, String id2) {
        List<Match> matches = new ArrayList<>(result.getMatches());
        SimilarityMetric currentMetric = new metrics.SymmetricSimilarity();
        printEditState(result, matches, currentMetric, id1, id2);
        while (true) {
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            try {
                if ("exit".equals(command)) {
                    updateAnalysisResult(result, matches, id1, id2);
                    break;
                } else if ("matches".equals(command)) {
                    printMatches(matches, result, id1, id2);
                } else if ("print".equals(command)) {
                    handlePrintCommand(parts, matches, result, id1, id2);
                } else if ("add".equals(command)) {
                    handleAddCommand(parts, matches, result);
                } else if ("discard".equals(command)) {
                    handleDiscardCommand(parts, matches, result, id1, id2);
                } else if ("extend".equals(command)) {
                    handleExtendCommand(parts, matches, result, id1, id2);
                } else if ("truncate".equals(command)) {
                    handleTruncateCommand(parts, matches, result, id1, id2);
                } else if ("set".equals(command)) {
                    currentMetric = handleSetCommand(parts);
                } else {
                    System.out.println("ERROR: Unknown command in edit mode: " + command);
                    continue;
                }
                if (!"exit".equals(command)) {
                    printEditState(result, matches, currentMetric, id1, id2);
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                printEditState(result, matches, currentMetric, id1, id2);
            }
        }
    }
    /**
     * Prints the current edit state.
     */
    private void printEditState(MatchResult result, List<Match> matches, SimilarityMetric metric, String id1, String id2) {
        MatchResult tempResult = new MatchResult(
                result.getText1(), result.getText2(),
                result.getSequence1(), result.getSequence2(),
                matches, result.getTokenizationStrategy(), result.getMinMatchLength()
        );
        double similarity = metric.calculate(tempResult);
        String formattedSimilarity = metric.format(similarity);
        System.out.println("Comparison of " + id1 + ", " + id2 + ": "
                + formattedSimilarity + " similarity, " + matches.size()
                + " matches. Available commands: matches, print, add, extend, truncate, discard, set, exit.");
    }
    /**
     * Prints all matches sorted by position in first text of the command.
     * Note: Match indices for commands are 1-based!
     */
    private void printMatches(List<Match> matches, MatchResult result, String id1, String id2) {
        boolean needSwap = !result.getText1().identifier().equals(id1);
        List<Match> sortedMatches = new ArrayList<>(matches);
        if (needSwap) {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence2));
        } else {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence1));
        }
        for (int i = 0; i < sortedMatches.size(); i++) {
            Match match = sortedMatches.get(i);
            if (needSwap) {
                System.out.println("Match of length " + match.length() + ": "
                        + match.startPosSequence2() + "-" + match.startPosSequence1());
            } else {
                System.out.println("Match of length " + match.length() + ": "
                        + match.startPosSequence1() + "-" + match.startPosSequence2());
            }
        }
    }

    /**
     * Gets a match by 1-based index, sorted by position in first text of command.
     */
    private Match getMatchByIndex(List<Match> matches, int oneBasedIndex, MatchResult result, String id1) {
        boolean needSwap = !result.getText1().identifier().equals(id1);
        List<Match> sortedMatches = new ArrayList<>(matches);
        if (needSwap) {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence2));
        } else {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence1));
        }

        if (oneBasedIndex < 1 || oneBasedIndex > sortedMatches.size()) {
            throw new RuntimeException("Invalid match index: " + oneBasedIndex);
        }
        return sortedMatches.get(oneBasedIndex - 1);
    }
    /**
     * Gets the index of a match in the original list.
     */
    private int getOriginalIndex(List<Match> matches, Match targetMatch) {
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).equals(targetMatch)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handles the print command.
     */
    private void handlePrintCommand(String[] parts, List<Match> matches, MatchResult result, String id1, String id2) {
        if (parts.length < 2 || parts.length > 3) {
            throw new RuntimeException("print command requires 1 or 2 arguments: print <n> [<number>]");
        }
        int matchIndex = Integer.parseInt(parts[1]);
        int contextSize = (parts.length == 3) ? Integer.parseInt(parts[2]) : 0;
        Match match = getMatchByIndex(matches, matchIndex, result, id1);
        boolean needSwap = !result.getText1().identifier().equals(id1);
        String firstText = needSwap ? result.getText2().content() : result.getText1().content();
        String secondText = needSwap ? result.getText1().content() : result.getText2().content();
        List<core.Token> firstSeq = needSwap ? result.getSequence2() : result.getSequence1();
        List<core.Token> secondSeq = needSwap ? result.getSequence1() : result.getSequence2();
        int firstPos = needSwap ? match.startPosSequence2() : match.startPosSequence1();
        int secondPos = needSwap ? match.startPosSequence1() : match.startPosSequence2();
        if (firstPos < firstSeq.size() && secondPos < secondSeq.size()) {
            int startChar1 = firstSeq.get(firstPos).getStartPosition();
            int endChar1 = firstSeq.get(Math.min(firstPos + match.length() - 1, firstSeq.size() - 1)).getEndPosition();
            int startChar2 = secondSeq.get(secondPos).getStartPosition();
            int endChar2 = secondSeq.get(Math.min(secondPos + match.length() - 1, secondSeq.size() - 1)).getEndPosition();
            String context1 = extractContext(firstText, startChar1, endChar1, contextSize);
            String context2 = extractContext(secondText, startChar2, endChar2, contextSize);
            System.out.println(context1);
            System.out.println("^".repeat(endChar1 - startChar1));
            System.out.println(context2);
            System.out.println("^".repeat(endChar2 - startChar2));
        }
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
     * Handles the add command.
     */
    private void handleAddCommand(String[] parts, List<Match> matches, MatchResult result) {
        if (parts.length != 4) {
            throw new RuntimeException("add command requires 3 arguments: add <t1> <t2> <len>");
        }

        int t1 = Integer.parseInt(parts[1]);
        int t2 = Integer.parseInt(parts[2]);
        int len = Integer.parseInt(parts[3]);

        Match newMatch = new Match(t1, t2, len);

        for (Match existing : matches) {
            if (newMatch.overlapsWith(existing)) {
                throw new RuntimeException("New match would overlap with existing match");
            }
        }
        matches.add(newMatch);
    }

    /**
     * Handles the discard command.
     */
    private void handleDiscardCommand(String[] parts, List<Match> matches, MatchResult result, String id1, String id2) {
        if (parts.length != 2) {
            throw new RuntimeException("discard command requires 1 argument: discard <n>");
        }

        int matchIndex = Integer.parseInt(parts[1]);

        Match targetMatch = getMatchByIndex(matches, matchIndex, result, id1);
        int originalIndex = getOriginalIndex(matches, targetMatch);

        if (originalIndex == -1) {
            throw new RuntimeException("Match not found");
        }
        matches.remove(originalIndex);
    }

    /**
     * Handles the extend command.
     */
    private void handleExtendCommand(String[] parts, List<Match> matches, MatchResult result, String id1, String id2) {
        if (parts.length != 3) {
            throw new RuntimeException("extend command requires 2 arguments: extend <n> <len>");
        }

        int displayIndex = Integer.parseInt(parts[1]); // 1-based index
        int len = Integer.parseInt(parts[2]);

        if (len == 0) {
            throw new RuntimeException("Extension length cannot be 0");
        }

        Match targetMatch = getMatchByIndex(matches, displayIndex, result, id1);
        int originalIndex = getOriginalIndex(matches, targetMatch);

        if (originalIndex == -1) {
            throw new RuntimeException("Match not found");
        }

        Match oldMatch = matches.get(originalIndex);
        Match newMatch;

        if (len > 0) {
            newMatch = new Match(oldMatch.startPosSequence1(),
                    oldMatch.startPosSequence2(),
                    oldMatch.length() + len);
        } else {
            newMatch = new Match(oldMatch.startPosSequence1() + len,
                    oldMatch.startPosSequence2() + len,
                    oldMatch.length() - len);
        }
        for (int i = 0; i < matches.size(); i++) {
            if (i != originalIndex && newMatch.overlapsWith(matches.get(i))) {
                throw new RuntimeException("Extended match would overlap with existing match");
            }
        }
        matches.set(originalIndex, newMatch);
    }

    /**
     * Handles the truncate command.
     */
    private void handleTruncateCommand(String[] parts, List<Match> matches, MatchResult result, String id1, String id2) {
        if (parts.length != 3) {
            throw new RuntimeException("truncate command requires 2 arguments: truncate <n> <len>");
        }

        int displayIndex = Integer.parseInt(parts[1]);
        int len = Integer.parseInt(parts[2]);

        if (len == 0) {
            throw new RuntimeException("Truncation length cannot be 0");
        }

        Match targetMatch = getMatchByIndex(matches, displayIndex, result, id1);
        int originalIndex = getOriginalIndex(matches, targetMatch);

        if (originalIndex == -1) {
            throw new RuntimeException("Match not found");
        }

        Match oldMatch = matches.get(originalIndex);
        Match newMatch;

        if (len > 0) {
            newMatch = new Match(oldMatch.startPosSequence1(),
                    oldMatch.startPosSequence2(),
                    Math.max(1, oldMatch.length() - len));
        } else {
            newMatch = new Match(oldMatch.startPosSequence1() - len,
                    oldMatch.startPosSequence2() - len,
                    Math.max(1, oldMatch.length() + len));
        }
        matches.set(originalIndex, newMatch);
    }

    /**
     * Handles the set command.
     */
    private SimilarityMetric handleSetCommand(String[] parts) {
        if (parts.length != 2) {
            throw new RuntimeException("set command requires 1 argument: set <metric>");
        }

        String metricName = parts[1].toUpperCase();
        SimilarityMetric metric = MetricFactory.createMetric(metricName);

        if (metric == null) {
            throw new RuntimeException("Unknown metric: " + metricName);
        }

        return metric;
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
        return "edit";
    }

    @Override
    public String getUsage() {
        return "edit <id> <id> - Enter interactive edit mode for a text pair";
    }
}