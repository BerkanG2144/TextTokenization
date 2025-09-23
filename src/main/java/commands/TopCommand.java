package commands;

import core.AnalysisResult;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to show the top N most similar text pairs.
 * Usage: top <metric> <n>
 *
 * @author [Dein u-Kürzel]
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
            throw new CommandException("top command requires exactly two arguments: top <metric> <n>");
        }

        String metricName = args[0].toUpperCase();
        int topN;

        try {
            topN = Integer.parseInt(args[1]);
            if (topN <= 0) {
                throw new CommandException("Number of results must be positive");
            }
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for top N");
        }

        // Get metric
        SimilarityMetric metric = MetricFactory.createMetric(metricName);
        if (metric == null) {
            throw new CommandException("Unknown metric: " + metricName +
                    ". Available metrics: AVG, MAX, MIN, LEN, LONG");
        }

        // Get analysis results
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }

        // Calculate similarities and sort
        List<SimilarityEntry> entries = new ArrayList<>();
        for (MatchResult result : analysisResult.getAllResults()) {
            double value = metric.calculate(result);
            String formattedValue = metric.format(value);

            String line = result.getText1().getIdentifier() + "-" +
                    result.getText2().getIdentifier() + ": " + formattedValue;

            entries.add(new SimilarityEntry(line, value,
                    result.getText1().getIdentifier(),
                    result.getText2().getIdentifier()));
        }

        // Sort by similarity value (descending), then by identifiers (ascending)
        entries.sort(new Comparator<SimilarityEntry>() {
            @Override
            public int compare(SimilarityEntry e1, SimilarityEntry e2) {
                // First by similarity value (descending)
                int valueCompare = Double.compare(e2.value, e1.value);
                if (valueCompare != 0) {
                    return valueCompare;
                }

                // Then by first identifier (ascending)
                int id1Compare = e1.id1.compareTo(e2.id1);
                if (id1Compare != 0) {
                    return id1Compare;
                }

                // Finally by second identifier (ascending)
                return e1.id2.compareTo(e2.id2);
            }
        });

        // Take only top N entries
        int actualN = Math.min(topN, entries.size());

        // Build output
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