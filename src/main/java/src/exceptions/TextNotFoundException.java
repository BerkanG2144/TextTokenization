package src.exceptions;

/**
 * Exception thrown when a text with a specific identifier cannot be found.
 * This exception is used when trying to access a text that doesn't exist
 * in the TextManager.
 *
 * @author ujnaa
 */
public class TextNotFoundException extends Exception {

    private final String identifier;

    /**
     * Constructs a new TextNotFoundException with the specified identifier.
     *
     * @param identifier the identifier of the text that was not found
     */
    public TextNotFoundException(String identifier) {
        super("Text with identifier '" + identifier + "' not found");
        this.identifier = identifier;
    }

    /**
     * Constructs a new TextNotFoundException with the specified identifier and message.
     *
     * @param identifier the identifier of the text that was not found
     * @param message the detail message explaining the reason for the exception
     */
    public TextNotFoundException(String identifier, String message) {
        super(message);
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of the text that was not found.
     *
     * @return the text identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}
