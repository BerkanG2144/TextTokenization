package commands;

import core.AnalysisResult;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to list similarity values for all text pairs.
 * Usage: list <metric> [order]
 *
 * @author [Dein u-Kürzel]
 */
public class ListCommand implements Command {
    private final AnalyzeCommand analyzeCommand;

    /**
     * Creates a new ListCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     */
    public ListCommand(AnalyzeCommand analyzeCommand) {
        this.analyzeCommand = analyzeCommand;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length < 1 || args.length > 2) {
            throw new CommandException("list command requires one or two arguments: list <metric> [order]");
        }

        String metricName = args[0].toUpperCase();
        String order = (args.length == 2) ? args[1].toUpperCase() : "DSC";

        // Validate order
        if (!order.equals("ASC") && !order.equals("DSC")) {
            throw new CommandException("Order must be ASC or DSC");
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

            // Format: searchText-patternText: value
            String line = result.getText1().getIdentifier() + "-" +
                    result.getText2().getIdentifier() + ": " + formattedValue;

            entries.add(new SimilarityEntry(line, value,
                    result.getText1().getIdentifier(),
                    result.getText2().getIdentifier()));
        }

        // Sort entries
        sortEntries(entries, order);

        // Build output
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                output.append("\n");
            }
            output.append(entries.get(i).line);
        }

        return output.toString();
    }

    /**
     * Sorts similarity entries according to the specified order.
     *
     * @param entries the entries to sort
     * @param order "ASC" or "DSC"
     */
    private void sortEntries(List<SimilarityEntry> entries, String order) {
        Comparator<SimilarityEntry> comparator = (e1, e2) -> {
            // First sort by similarity value
            int valueCompare = Double.compare(e1.value, e2.value);
            if (valueCompare != 0) {
                return order.equals("ASC") ? valueCompare : -valueCompare;
            }

            // Then by first identifier (alphabetically ascending)
            int id1Compare = e1.id1.compareTo(e2.id1);
            if (id1Compare != 0) {
                return id1Compare;
            }

            // Finally by second identifier (alphabetically ascending)
            return e1.id2.compareTo(e2.id2);
        };

        entries.sort(comparator);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "list <metric> [order] - List similarity values for all text pairs";
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