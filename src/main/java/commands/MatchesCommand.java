package commands;

import core.AnalysisResult;
import core.Match;
import matching.MatchResult;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to list matches for a specific text pair.
 * Usage: matches <id> <id>
 *
 * @author [Dein u-Kürzel]
 */
public class MatchesCommand implements Command {
    private final AnalyzeCommand analyzeCommand;

    /**
     * Creates a new MatchesCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     */
    public MatchesCommand(AnalyzeCommand analyzeCommand) {
        this.analyzeCommand = analyzeCommand;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("matches command requires exactly two arguments: matches <id> <id>");
        }

        String id1 = args[0];
        String id2 = args[1];

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

        List<Match> matches = result.getMatches();
        if (matches.isEmpty()) {
            return ""; // No matches found
        }

        // Sort matches according to specification:
        // 1. Descending by length
        // 2. Ascending by start index in search sequence
        // 3. Ascending by start index in pattern sequence
        List<Match> sortedMatches = new ArrayList<>(matches);
        sortedMatches.sort(new Comparator<Match>() {
            @Override
            public int compare(Match m1, Match m2) {
                // First by length (descending)
                int lengthCompare = Integer.compare(m2.getLength(), m1.getLength());
                if (lengthCompare != 0) {
                    return lengthCompare;
                }

                // Then by start position in search sequence (ascending)
                int pos1Compare = Integer.compare(m1.getStartPosSequence1(), m2.getStartPosSequence1());
                if (pos1Compare != 0) {
                    return pos1Compare;
                }

                // Finally by start position in pattern sequence (ascending)
                return Integer.compare(m1.getStartPosSequence2(), m2.getStartPosSequence2());
            }
        });

        // Format output
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (i > 0) {
                output.append("\n");
            }

            Match match = sortedMatches.get(i);

            // Format: Match of length <n>: <t1> - <t2>
            // t1 = start index in search sequence
            // t2 = start index in pattern sequence
            output.append("Match of length ")
                    .append(match.getLength())
                    .append(": ")
                    .append(match.getStartPosSequence1())
                    .append("-")
                    .append(match.getStartPosSequence2());
        }

        return output.toString();
    }

    @Override
    public String getName() {
        return "matches";
    }

    @Override
    public String getUsage() {
        return "matches <id> <id> - List matches for a specific text pair";
    }
}