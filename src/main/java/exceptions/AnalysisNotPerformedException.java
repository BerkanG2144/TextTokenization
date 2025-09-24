package exceptions;
/**
 * Exception thrown when trying to access analysis results
 * before performing an analysis.
 *
 * @author ujnaa
 */
public class AnalysisNotPerformedException extends Exception {

    /**
     * Constructs a new AnalysisNotPerformedException with a default message.
     */
    public AnalysisNotPerformedException() {
        super("No analysis results available. Please run analyze command first.");
    }

    /**
     * Constructs a new AnalysisNotPerformedException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AnalysisNotPerformedException(String message) {
        super(message);
    }
}