package metrics;

/**
 * Factory class for creating similarity metrics.
 *
 * @author ujnaa
 */
public final class MetricFactory {

    private MetricFactory() {
        //
    }

    /**
     * Creates a similarity metric by name.
     *
     * @param metricName the metric name (case insensitive)
     * @return the metric instance, or null if not found
     */
    public static SimilarityMetric createMetric(String metricName) {
        if (metricName == null) {
            return null;
        }

        switch (metricName.toUpperCase()) {
            case "AVG":
                return new SymmetricSimilarity();
            case "MAX":
                return new MaximalSimilarity();
            case "MIN":
                return new MinimalSimilarity();
            case "LONG":
                return new LongestMatch();
            case "LEN":
                return new MatchSum();
            default:
                return null;
        }
    }

    /**
     * Gets all available metric names.
     *
     * @return array of metric names
     */
    public static String[] getAvailableMetrics() {
        return new String[]{"AVG", "MAX", "MIN", "LONG", "LEN"};
    }
}