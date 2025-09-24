package exceptions;

/**
 * Exception thrown when an invalid or unsupported tokenization strategy is used.
 *
 * @author ujnaa
 */
public class InvalidTokenizationStrategyException extends Exception {

    private final String strategyName;

    /**
     * Constructs a new InvalidTokenizationStrategyException with the specified strategy name.
     *
     * @param strategyName the name of the invalid strategy
     */
    public InvalidTokenizationStrategyException(String strategyName) {
        super("Unknown tokenization strategy: '" + strategyName + "'. " + "Available strategies: CHAR, WORD, SMART");
        this.strategyName = strategyName;
    }

    /**
     * Gets the name of the invalid strategy.
     *
     * @return the strategy name
     */
    public String getStrategyName() {
        return strategyName;
    }
}