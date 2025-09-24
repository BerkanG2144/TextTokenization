package commands;

import core.AnalysisResult;
import exceptions.CommandException;
import matching.MatchResult;
import metrics.SimilarityMetric;
import metrics.MetricFactory;

/**
 * Command to display a histogram of similarity values.
 *
 * @author ujnaa
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
        SimilarityMetric metric = MetricFactory.createMetric(metricName);
        if (metric == null) {
            throw new CommandException("Unknown metric: " + metricName
                    + ". Available metrics: AVG, MAX, MIN, LEN, LONG");
        }
        if (!metric.isPercentage()) {
            throw new CommandException("Histogram is only available for percentage metrics (AVG, MAX, MIN)");
        }
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new CommandException("No analysis results available. Run analyze command first.");
        }
        int[] bins = new int[10];
        for (MatchResult result : analysisResult.getAllResults()) {
            double value = metric.calculate(result);
            int binIndex;
            if (value >= 100.0) {
                binIndex = 9;
            } else {
                binIndex = (int) (value / 10.0);
            }
            binIndex = Math.max(0, Math.min(9, binIndex));
            bins[binIndex]++;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 9; i >= 0; i--) {
            if (output.length() > 0) {
                output.append("\n");
            }
            output.append(":");
            for (int j = 0; j < bins[i]; j++) {
                output.append("|");
            }
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