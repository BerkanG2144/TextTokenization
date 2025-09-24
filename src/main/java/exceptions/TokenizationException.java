package exceptions;

/**
 * Exception thrown when tokenization fails.
 *
 * @author ujnaa
 */
public class TokenizationException extends Exception {

    private final String text;
    private final String strategy;

    /**
     * Constructs a new TokenizationException with the specified details.
     *
     * @param text the text that failed to tokenize (truncated if too long)
     * @param strategy the tokenization strategy that was used
     * @param cause the underlying cause
     */
    public TokenizationException(String text, String strategy, Throwable cause) {
        super("Failed to tokenize text using " + strategy + " strategy: " + truncateText(text, 50), cause);
        this.text = text;
        this.strategy = strategy;
    }

    /**
     * Convenience constructor without cause.
     *
     * @param text the text that failed to tokenize
     * @param strategy the strategy used
     */
    public TokenizationException(String text, String strategy) {
        super("Failed to tokenize text using " + strategy + " strategy: " + truncateText(text, 50));
        this.text = text;
        this.strategy = strategy;
    }

    /**
     * Convenience constructor with only a message (for general use).
     *
     * @param message the error message
     */
    public TokenizationException(String message) {
        super(message);
        this.text = null;
        this.strategy = null;
    }

    /**
     * Truncates text for error messages.
     *
     * @param text the text to truncate
     * @param maxLength the maximum allowed length
     * @return truncated text
     */
    private static String truncateText(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Gets the text that failed to tokenize.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }
}
