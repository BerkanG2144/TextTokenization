package commands;

import core.AnalysisResult;
import core.Match;
import exceptions.CommandException;
import exceptions.AnalysisNotPerformedException;
import exceptions.InvalidMatchException;
import exceptions.InvalidMetricException;
import exceptions.TextNotFoundException;
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
    public String execute(String[] args)
            throws CommandException, TextNotFoundException, AnalysisNotPerformedException {
        if (args.length != 2) {
            throw new CommandException("ERROR: edit command requires exactly two arguments: edit <id> <id>");
        }

        String id1 = args[0];
        String id2 = args[1];

        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new AnalysisNotPerformedException();
        }

        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new TextNotFoundException(id1 + "/" + id2,
                    "ERROR: No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        enterEditMode(result, id1, id2);
        return "OK, exit editing mode.";
    }

    /**
     * Enters the interactive edit mode.
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
                    handleAddCommand(parts, matches);
                } else if ("discard".equals(command)) {
                    handleDiscardCommand(parts, matches, result, id1);
                } else if ("extend".equals(command)) {
                    handleExtendCommand(parts, matches, result, id1);
                } else if ("truncate".equals(command)) {
                    handleTruncateCommand(parts, matches, result, id1);
                } else if ("set".equals(command)) {
                    currentMetric = handleSetCommand(parts);
                } else {
                    System.out.println("ERROR: Unknown command in edit mode: " + command);
                    continue;
                }

                if (!"exit".equals(command)) {
                    printEditState(result, matches, currentMetric, id1, id2);
                }

            } catch (CommandException | InvalidMatchException | InvalidMetricException e) {
                System.out.println(e.getMessage());
                printEditState(result, matches, currentMetric, id1, id2);
            }
        }
    }

    private void printEditState(MatchResult result, List<Match> matches,
                                SimilarityMetric metric, String id1, String id2) {
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

    private void printMatches(List<Match> matches, MatchResult result, String id1, String id2) {
        boolean needSwap = !result.getText1().identifier().equals(id1);
        List<Match> sortedMatches = new ArrayList<>(matches);
        if (needSwap) {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence2));
        } else {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence1));
        }
        for (Match match : sortedMatches) {
            if (needSwap) {
                System.out.println("Match of length " + match.length() + ": "
                        + match.startPosSequence2() + "-" + match.startPosSequence1());
            } else {
                System.out.println("Match of length " + match.length() + ": "
                        + match.startPosSequence1() + "-" + match.startPosSequence2());
            }
        }
    }

    private Match getMatchByIndex(List<Match> matches, int oneBasedIndex,
                                  MatchResult result, String id1) throws CommandException {
        boolean needSwap = !result.getText1().identifier().equals(id1);
        List<Match> sortedMatches = new ArrayList<>(matches);
        if (needSwap) {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence2));
        } else {
            sortedMatches.sort(Comparator.comparingInt(Match::startPosSequence1));
        }

        if (oneBasedIndex < 1 || oneBasedIndex > sortedMatches.size()) {
            throw new CommandException("ERROR: Invalid match index: " + oneBasedIndex + ". Valid range: 1-" + sortedMatches.size());
        }
        return sortedMatches.get(oneBasedIndex - 1);
    }

    private int getOriginalIndex(List<Match> matches, Match targetMatch) {
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).equals(targetMatch)) {
                return i;
            }
        }
        return -1;
    }

    private void handlePrintCommand(String[] parts, List<Match> matches,
                                    MatchResult result, String id1, String id2)
            throws CommandException {
        if (parts.length < 2 || parts.length > 3) {
            throw new CommandException("ERROR: print command requires 1 or 2 arguments: print <n> [<context_size>]");
        }

        try {
            int matchIndex = Integer.parseInt(parts[1]);
            int contextSize = (parts.length == 3) ? Integer.parseInt(parts[2]) : 0;

            if (contextSize < 0) {
                throw new CommandException("ERROR: Context size must be non-negative, got: " + contextSize);
            }

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
        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in print command: " + e.getMessage());
        }
    }

    private String extractContext(String text, int start, int end, int contextSize) {
        int contextStart = Math.max(0, start - contextSize);
        int contextEnd = Math.min(text.length(), end + contextSize);
        StringBuilder result = new StringBuilder();
        if (contextStart > 0) {
            result.append("...");
        }
        result.append(text, contextStart, contextEnd);
        if (contextEnd < text.length()) {
            result.append("...");
        }
        return result.toString();
    }

    private void handleAddCommand(String[] parts, List<Match> matches)
            throws CommandException, InvalidMatchException {
        if (parts.length != 4) {
            throw new CommandException("ERROR: add command requires 3 arguments: add <t1> <t2> <len>");
        }

        try {
            int t1 = Integer.parseInt(parts[1]);
            int t2 = Integer.parseInt(parts[2]);
            int len = Integer.parseInt(parts[3]);

            if (len <= 0) {
                throw new InvalidMatchException("ERROR: Match length must be positive, got: " + len);
            }
            if (t1 < 0 || t2 < 0) {
                throw new InvalidMatchException("ERROR: Match positions must be non-negative.");
            }

            Match newMatch = new Match(t1, t2, len);
            for (Match existing : matches) {
                if (newMatch.overlapsWith(existing)) {
                    throw InvalidMatchException.forOverlap(
                            "ERROR: Overlap detected with ("
                                    + newMatch.startPosSequence1() + "," + newMatch.startPosSequence2() + ","
                                    + newMatch.length() + ")");
                }
            }
            matches.add(newMatch);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in add command: " + e.getMessage());
        }
    }

    private void handleDiscardCommand(String[] parts, List<Match> matches,
                                      MatchResult result, String id1) throws CommandException {
        if (parts.length != 2) {
            throw new CommandException("ERROR: discard command requires 1 argument: discard <n>");
        }

        try {
            int matchIndex = Integer.parseInt(parts[1]);
            Match targetMatch = getMatchByIndex(matches, matchIndex, result, id1);
            int originalIndex = getOriginalIndex(matches, targetMatch);
            if (originalIndex == -1) {
                throw new CommandException("ERROR: Match not found in list");
            }
            matches.remove(originalIndex);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in discard command: " + e.getMessage());
        }
    }

    private void handleExtendCommand(String[] parts, List<Match> matches,
                                     MatchResult result, String id1) throws CommandException, InvalidMatchException {
        if (parts.length != 3) {
            throw new CommandException("ERROR: extend command requires 2 arguments: extend <n> <len>");
        }

        try {
            int displayIndex = Integer.parseInt(parts[1]);
            int len = Integer.parseInt(parts[2]);

            if (len == 0) {
                throw new CommandException("ERROR: Extension length cannot be 0");
            }

            Match targetMatch = getMatchByIndex(matches, displayIndex, result, id1);
            int originalIndex = getOriginalIndex(matches, targetMatch);
            if (originalIndex == -1) {
                throw new CommandException("ERROR: Match not found in list");
            }

            Match oldMatch = matches.get(originalIndex);
            Match newMatch;

            if (len > 0) {
                newMatch = new Match(oldMatch.startPosSequence1(),
                        oldMatch.startPosSequence2(),
                        oldMatch.length() + len);
            } else {
                int newStart1 = oldMatch.startPosSequence1() + len;
                int newStart2 = oldMatch.startPosSequence2() + len;
                int newLength = oldMatch.length() - len;
                if (newStart1 < 0 || newStart2 < 0) {
                    throw new CommandException("ERROR: Extension would result in negative start positions");
                }
                if (newLength <= 0) {
                    throw new CommandException("ERROR: Extension would result in zero or negative length");
                }
                newMatch = new Match(newStart1, newStart2, newLength);
            }

            for (int i = 0; i < matches.size(); i++) {
                if (i != originalIndex && newMatch.overlapsWith(matches.get(i))) {
                    throw InvalidMatchException.forOverlap("ERROR: extended match overlaps with another match");
                }
            }
            matches.set(originalIndex, newMatch);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in extend command: " + e.getMessage());
        }
    }

    private void handleTruncateCommand(String[] parts, List<Match> matches,
                                       MatchResult result, String id1) throws CommandException {
        if (parts.length != 3) {
            throw new CommandException("ERROR: truncate command requires 2 arguments: truncate <n> <len>");
        }

        try {
            int displayIndex = Integer.parseInt(parts[1]);
            int len = Integer.parseInt(parts[2]);

            if (len == 0) {
                throw new CommandException("ERROR: Truncation length cannot be 0");
            }

            Match targetMatch = getMatchByIndex(matches, displayIndex, result, id1);
            int originalIndex = getOriginalIndex(matches, targetMatch);
            if (originalIndex == -1) {
                throw new CommandException("ERROR: Match not found in list");
            }

            Match oldMatch = matches.get(originalIndex);
            Match newMatch;

            if (len > 0) {
                int newLength = Math.max(1, oldMatch.length() - len);
                newMatch = new Match(oldMatch.startPosSequence1(),
                        oldMatch.startPosSequence2(),
                        newLength);
            } else {
                int newStart1 = oldMatch.startPosSequence1() - len;
                int newStart2 = oldMatch.startPosSequence2() - len;
                int newLength = Math.max(1, oldMatch.length() + len);
                if (newStart1 < 0 || newStart2 < 0) {
                    throw new CommandException("ERROR: Truncation would result in negative start positions");
                }
                newMatch = new Match(newStart1, newStart2, newLength);
            }

            matches.set(originalIndex, newMatch);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in truncate command: " + e.getMessage());
        }
    }

    private SimilarityMetric handleSetCommand(String[] parts)
            throws CommandException, InvalidMetricException {
        if (parts.length != 2) {
            throw new CommandException("ERROR: set command requires 1 argument: set <metric>");
        }

        String metricName = parts[1].toUpperCase();
        SimilarityMetric metric = MetricFactory.createMetric(metricName);
        if (metric == null) {
            throw new InvalidMetricException("ERROR: Unknown metric: " + metricName);
        }
        return metric;
    }

    private void updateAnalysisResult(MatchResult originalResult, List<Match> modifiedMatches,
                                      String id1, String id2) {
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
