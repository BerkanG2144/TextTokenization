package commands;

import core.AnalysisResult;
import exceptions.CommandException;
import matching.MatchResult;
import metrics.MetricFactory;
import metrics.SimilarityMetric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Command to show the top N most similar text pairs.
 *
 * @author ujnaa
 */
public class TopCommand implements Command {
    private final AnalyzeCommand analyzeCommand;

    /**
     * Creates a new TopCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     */
    public TopCommand(AnalyzeCommand analyzeCommand) {
        this.analyzeCommand = analyzeCommand;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("top command requires exactly two arguments: top <n> <metric>");
        }
        int topN;
        try {
            topN = Integer.parseInt(args[0]); // First argument is N
            if (topN <= 0) {
                throw new CommandException("Number of results must be positive");
            }
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for top N");
        }
        String metricName = args[1].toUpperCase(); // Second argument is metric
        SimilarityMetric metric = MetricFactory.createMetric(metricName);
        if (metric == null) {
            throw new CommandException("Unknown metric: " + metricName + ". Available metrics: AVG, MAX, MIN, LEN, LONG");
        }
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }
        List<SimilarityEntry> entries = new ArrayList<>();
        for (MatchResult result : analysisResult.getAllResults()) {
            double value = metric.calculate(result);
            String formattedValue = metric.format(value);
            String searchId = result.getSearchText().identifier();
            String patternId = result.getPatternText().identifier();
            String line = searchId + "-" + patternId + ": " + formattedValue;
            entries.add(new SimilarityEntry(line, value, searchId, patternId));
        }
        entries.sort(new Comparator<SimilarityEntry>() {
            @Override
            public int compare(SimilarityEntry e1, SimilarityEntry e2) {
                int valueCompare = Double.compare(e2.value, e1.value); // Descending
                if (valueCompare != 0) {
                    return valueCompare;
                }
                int id1Compare = e1.id1.compareTo(e2.id1); // Ascending
                if (id1Compare != 0) {
                    return id1Compare;
                }
                return e1.id2.compareTo(e2.id2); // Ascending
            }
        });
        int actualN = Math.min(topN, entries.size());
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < actualN; i++) {
            if (i > 0) {
                output.append("\n");
            }
            output.append(entries.get(i).line);
        }
        return output.toString();
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getUsage() {
        return "top <metric> <n> - Show top N most similar text pairs";
    }

    /**
     * Helper class for sorting similarity entries.
     */
    private static class SimilarityEntry {
        final String line;
        final double value;
        final String id1;
        final String id2;

        SimilarityEntry(String line, double value, String id1, String id2) {
            this.line = line;
            this.value = value;
            this.id1 = id1;
            this.id2 = id2;
        }
    }
}