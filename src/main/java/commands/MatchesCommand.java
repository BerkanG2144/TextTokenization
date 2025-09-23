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

        List<Match> sortedMatches = new ArrayList<>(matches);

// Bestimme ob text1 search text ist
        boolean text1IsSearch = result.getText1().getIdentifier().equals(result.getSearchText().getIdentifier());

        sortedMatches.sort(new Comparator<Match>() {
            @Override
            public int compare(Match m1, Match m2) {
                // First by length (descending)
                int lengthCompare = Integer.compare(m2.getLength(), m1.getLength());
                if (lengthCompare != 0) {
                    return lengthCompare;
                }

                // Get search positions for both matches
                int searchPos1 = text1IsSearch ? m1.getStartPosSequence1() : m1.getStartPosSequence2();
                int searchPos2 = text1IsSearch ? m2.getStartPosSequence1() : m2.getStartPosSequence2();

                // Then by start position in search sequence (ascending)
                int searchCompare = Integer.compare(searchPos1, searchPos2);
                if (searchCompare != 0) {
                    return searchCompare;
                }

                // Get pattern positions for both matches
                int patternPos1 = text1IsSearch ? m1.getStartPosSequence2() : m1.getStartPosSequence1();
                int patternPos2 = text1IsSearch ? m2.getStartPosSequence2() : m2.getStartPosSequence1();

                // Finally by start position in pattern sequence (ascending)
                return Integer.compare(patternPos1, patternPos2);
            }
        });

        // Format output
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (i > 0) {
                output.append("\n");
            }

            Match match = sortedMatches.get(i);

            // Bestimme Search und Pattern Positionen basierend auf MatchResult
            int searchPos, patternPos;

            if (result.getText1().getIdentifier().equals(result.getSearchText().getIdentifier())) {
                // text1 ist search text
                searchPos = match.getStartPosSequence1();
                patternPos = match.getStartPosSequence2();
            } else {
                // text2 ist search text
                searchPos = match.getStartPosSequence2();
                patternPos = match.getStartPosSequence1();
            }
            output.append("Match of length ")
                    .append(match.getLength())
                    .append(": ")
                    .append(searchPos)
                    .append("-")
                    .append(patternPos);
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