package commands;

import core.AnalysisResult;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;
import java.util.List;

/**
 * Command to display a histogram of similarity values.
 * Usage: histogram <metric>
 *
 * @author [Dein u-Kürzel]
 */
public class HistogramCommand implements Command {
    private final AnalyzeCommand analyzeCommand;

    /**
     * Creates a new HistogramCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     */
    public HistogramCommand(AnalyzeCommand analyzeCommand) {
        this.analyzeCommand = analyzeCommand;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("histogram command requires exactly one argument: histogram <metric>");
        }

        String metricName = args[0].toUpperCase();

        // Get metric
        SimilarityMetric metric = MetricFactory.createMetric(metricName);
        if (metric == null) {
            throw new CommandException("Unknown metric: " + metricName +
                    ". Available metrics: AVG, MAX, MIN, LEN, LONG");
        }

        // Check if metric returns percentage values
        if (!metric.isPercentage()) {
            throw new CommandException("Histogram is only available for percentage metrics (AVG, MAX, MIN)");
        }

        // Get analysis results
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }

        // Create histogram bins (10 classes: 0-9%, 10-19%, ..., 90-100%)
        int[] bins = new int[10];

        // Calculate similarities and categorize into bins
        for (MatchResult result : analysisResult.getAllResults()) {
            double value = metric.calculate(result);

            // Determine which bin this value belongs to
            int binIndex;
            if (value >= 100.0) {
                binIndex = 9; // 90-100% bin
            } else {
                binIndex = (int) (value / 10.0);
            }

            // Ensure bin index is within valid range
            binIndex = Math.max(0, Math.min(9, binIndex));
            bins[binIndex]++;
        }

        // Generate histogram output (bins in descending order)
        StringBuilder output = new StringBuilder();
        for (int i = 9; i >= 0; i--) {
            if (output.length() > 0) {
                output.append("\n");
            }

            output.append(":");

            // Add vertical bars
            for (int j = 0; j < bins[i]; j++) {
                output.append("|");
            }

            // Add space and count
            output.append(" ").append(bins[i]);
        }

        return output.toString();
    }

    @Override
    public String getName() {
        return "histogram";
    }

    @Override
    public String getUsage() {
        return "histogram <metric> - Display histogram of similarity values";
    }
}