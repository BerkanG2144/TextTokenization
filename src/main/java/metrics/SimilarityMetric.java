package metrics;

import matching.MatchResult;

/**
 * Interface for calculating similarity metrics between texts.
 *
 * @author ujnaa
 */
public interface SimilarityMetric {

    /**
     * Calculates the similarity value for a match result.
     *
     * @param result the match result to evaluate
     * @return the similarity value
     */
    double calculate(MatchResult result);

    /**
     * Gets the name of this metric.
     *
     * @return the metric name
     */
    String getName();

    /**
     * Checks if this metric returns percentage values.
     *
     * @return true if the metric returns percentages
     */
    boolean isPercentage();

    /**
     * Formats the metric value for display.
     *
     * @param value the calculated value
     * @return formatted string representation
     */
    String format(double value);
}