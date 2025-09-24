package commands;

import core.AnalysisResult;
import core.Match;
import exceptions.CommandException;
import matching.MatchResult;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to list matches for a specific text pair.
 *
 * @author ujnaa
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

        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }

        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new CommandException("No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        List<Match> matches = result.getMatches();
        if (matches.isEmpty()) {
            return "";
        }

        boolean text1IsSearch = result.getText1().identifier()
                .equals(result.getSearchText().identifier());
        List<Match> sortedMatches = new ArrayList<>(matches);
        sortedMatches.sort(createMatchComparator(text1IsSearch));

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (i > 0) {
                out.append('\n');
            }
            Match m = sortedMatches.get(i);
            int searchPos  = text1IsSearch ? m.startPosSequence1() : m.startPosSequence2();
            int patternPos = text1IsSearch ? m.startPosSequence2() : m.startPosSequence1();
            out.append("Match of length ")
                    .append(m.length())
                    .append(": ")
                    .append(searchPos)
                    .append("-")
                    .append(patternPos);
        }
        return out.toString();
    }

    private Comparator<Match> createMatchComparator(boolean text1IsSearch) {
        return (m1, m2) -> compareMatches(m1, m2, text1IsSearch);
    }

    private int compareMatches(Match m1, Match m2, boolean text1IsSearch) {
        int byLen = Integer.compare(m2.length(), m1.length());
        if (byLen != 0) {
            return byLen;
        }

        int s1 = text1IsSearch ? m1.startPosSequence1() : m1.startPosSequence2();
        int s2 = text1IsSearch ? m2.startPosSequence1() : m2.startPosSequence2();
        int bySearch = Integer.compare(s1, s2);
        if (bySearch != 0) {
            return bySearch;
        }

        int p1 = text1IsSearch ? m1.startPosSequence2() : m1.startPosSequence1();
        int p2 = text1IsSearch ? m2.startPosSequence2() : m2.startPosSequence1();
        return Integer.compare(p1, p2);
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