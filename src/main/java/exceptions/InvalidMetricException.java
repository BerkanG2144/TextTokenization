package exceptions;

/**
 * Exception thrown when an invalid or unsupported similarity metric is used.
 *
 * @author ujnaa
 */
public class InvalidMetricException extends Exception {

    private final String metricName;

    /**
     * Constructs a new InvalidMetricException with the specified metric name.
     *
     * @param metricName the name of the invalid metric
     */
    public InvalidMetricException(String metricName) {
        super("Unknown similarity metric: '" + metricName + "'. " + "Available metrics: AVG, MAX, MIN, LEN, LONG");
        this.metricName = metricName;
    }

    /**
     * Gets the name of the invalid metric.
     *
     * @return the metric name
     */
    public String getMetricName() {
        return metricName;
    }
}