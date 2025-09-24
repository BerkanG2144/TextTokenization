package commands;

import commands.inspect.ExclusionRegistry;
import core.AnalysisResult;
import exceptions.CommandException;
import matching.MatchResult;
import metrics.MetricFactory;
import metrics.SimilarityMetric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

            String searchId  = result.getSearchText().identifier();
            String patternId = result.getPatternText().identifier();

            // ▼ Neu: Exclusion berücksichtigen (Anzeige überschreiben, nicht neu berechnen)
            boolean excluded = ExclusionRegistry.getInstance().contains(searchId)
                    || ExclusionRegistry.getInstance().contains(patternId);
            double shownValue = excluded ? 0.0 : value;

            String formattedValue = metric.format(shownValue);

            String line = searchId + "-" + patternId + ": " + formattedValue;
            entries.add(new SimilarityEntry(line, shownValue, searchId, patternId));
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
        if (order.equals("ASC")) {
            // Ascending order: value ascending, then id1 ascending, then id2 ascending
            entries.sort(Comparator
                    .comparingDouble((SimilarityEntry e) -> e.value)
                    .thenComparing(e -> e.id1)
                    .thenComparing(e -> e.id2));
        } else { // DSC
            // Descending order: value descending, but still id1 ascending, then id2 ascending for ties
            entries.sort(Comparator
                    .comparingDouble((SimilarityEntry e) -> -e.value)  // Negative for descending value
                    .thenComparing(e -> e.id1)  // Always ascending for identifiers
                    .thenComparing(e -> e.id2)); // Always ascending for identifiers
        }
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