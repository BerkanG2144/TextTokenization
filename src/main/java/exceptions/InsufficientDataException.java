package exceptions;

/**
 * Exception thrown when there is insufficient data to perform an operation.
 * For example, when trying to analyze with fewer than 2 texts.
 *
 * @author ujnaa
 */
public class InsufficientDataException extends Exception {

    private final int required;
    private final int available;

    /**
     * Constructs a new InsufficientDataException with the specified requirements.
     *
     * @param required the minimum required amount
     * @param available the available amount
     * @param dataType the type of data (e.g., "texts", "matches")
     */
    public InsufficientDataException(int required, int available, String dataType) {
        super("Insufficient " + dataType + ": need at least " + required + " but only " + available + " available");
        this.required = required;
        this.available = available;
    }

    /**
     * Gets the required amount.
     *
     * @return the required amount
     */
    public int getRequired() {
        return required;
    }

    /**
     * Gets the available amount.
     *
     * @return the available amount
     */
    public int getAvailable() {
        return available;
    }
}