package commands;

import core.AnalysisResult;
import exceptions.CommandException;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to list similarity values for all text pairs.
 *
 * @author ujnaa
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
            throw new CommandException("Unknown metric: " + metricName + ". Available metrics: AVG, MAX, MIN, LEN, LONG");
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
            // The search text is the longer sequence (or first text if same length)
            String searchId = result.getSearchText().identifier();
            String patternId = result.getPatternText().identifier();
            String line = searchId + "-" + patternId + ": " + formattedValue;

            entries.add(new SimilarityEntry(line, value, searchId, patternId));
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
        Comparator<SimilarityEntry> comparator = Comparator
                .comparingDouble((SimilarityEntry e) -> e.value)
                .thenComparing(e -> e.id1)
                .thenComparing(e -> e.id2);

        if (order.equals("DSC")) {
            comparator = comparator.reversed()
                    .thenComparing(e -> e.id1)
                    .thenComparing(e -> e.id2);
        }

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