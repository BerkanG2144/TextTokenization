package commands.edit.handlers;

import core.Match;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;
import exceptions.CommandException;
import exceptions.InvalidMatchException;
import exceptions.InvalidMetricException;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Handles individual edit commands and operations.
 *
 * @author ujnaa
 */
public class EditCommandHandler {

    /**
     * Prints all matches for the current result.
     *
     * @param matches list of matches to print
     * @param result match result containing text data
     * @param id1 first text identifier
     * @param id2 second text identifier
     */
    public void printMatches(List<Match> matches, MatchResult result, String id1, String id2) {
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

    /**
     * Handles the print command to display a specific match with context.
     *
     * @param parts command parts from user input
     * @param matches list of matches to search in
     * @param result match result containing text data
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @throws CommandException if command arguments are invalid
     */
    public void handlePrintCommand(String[] parts, List<Match> matches,
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
            displayMatchWithContext(match, result, id1, contextSize);
        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in print command: " + e.getMessage());
        }
    }

    /**
     * Handles adding a new match.
     *
     * @param parts command parts from user input
     * @param matches list of matches to add to
     * @throws CommandException if command arguments are invalid
     * @throws InvalidMatchException if the new match is invalid or overlaps
     */
    public void handleAddCommand(String[] parts, List<Match> matches)
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

    /**
     * Handles discarding a match.
     *
     * @param parts command parts from user input
     * @param matches list of matches to remove from
     * @param result match result for index lookup
     * @param id1 first text identifier for sorting
     * @throws CommandException if command arguments are invalid or match not found
     */
    public void handleDiscardCommand(String[] parts, List<Match> matches,
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

    /**
     * Handles extending a match.
     *
     * @param parts command parts from user input
     * @param matches list of matches to modify
     * @param result match result for index lookup
     * @param id1 first text identifier for sorting
     * @throws CommandException if command arguments are invalid
     * @throws InvalidMatchException if extension would cause overlap
     */
    public void handleExtendCommand(String[] parts, List<Match> matches,
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
            Match newMatch = createExtendedMatch(oldMatch, len);

            validateNoOverlap(newMatch, matches, originalIndex);
            matches.set(originalIndex, newMatch);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in extend command: " + e.getMessage());
        }
    }

    /**
     * Handles truncating a match.
     *
     * @param parts command parts from user input
     * @param matches list of matches to modify
     * @param result match result for index lookup
     * @param id1 first text identifier for sorting
     * @throws CommandException if command arguments are invalid
     */
    public void handleTruncateCommand(String[] parts, List<Match> matches,
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
            Match newMatch = createTruncatedMatch(oldMatch, len);

            matches.set(originalIndex, newMatch);

        } catch (NumberFormatException e) {
            throw new CommandException("ERROR: Invalid number format in truncate command: " + e.getMessage());
        }
    }

    /**
     * Handles setting a new similarity metric.
     *
     * @param parts command parts from user input
     * @return the created similarity metric
     * @throws CommandException if command arguments are invalid
     * @throws InvalidMetricException if the metric name is unknown
     */
    public SimilarityMetric handleSetCommand(String[] parts)
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

    // Helper methods
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

    private void displayMatchWithContext(Match match, MatchResult result, String id1, int contextSize) {
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

    private Match createExtendedMatch(Match oldMatch, int len) throws CommandException {
        if (len > 0) {
            return new Match(oldMatch.startPosSequence1(),
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
            return new Match(newStart1, newStart2, newLength);
        }
    }

    private Match createTruncatedMatch(Match oldMatch, int len) throws CommandException {
        if (len > 0) {
            int newLength = Math.max(1, oldMatch.length() - len);
            return new Match(oldMatch.startPosSequence1(),
                    oldMatch.startPosSequence2(),
                    newLength);
        } else {
            int newStart1 = oldMatch.startPosSequence1() - len;
            int newStart2 = oldMatch.startPosSequence2() - len;
            int newLength = Math.max(1, oldMatch.length() + len);
            if (newStart1 < 0 || newStart2 < 0) {
                throw new CommandException("ERROR: Truncation would result in negative start positions");
            }
            return new Match(newStart1, newStart2, newLength);
        }
    }

    private void validateNoOverlap(Match newMatch, List<Match> matches, int excludeIndex)
            throws InvalidMatchException {
        for (int i = 0; i < matches.size(); i++) {
            if (i != excludeIndex && newMatch.overlapsWith(matches.get(i))) {
                throw InvalidMatchException.forOverlap("ERROR: extended match overlaps with another match");
            }
        }
    }
}