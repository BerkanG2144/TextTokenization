package commands.inspect;

/**
 * Parameters for the inspect command.
 * Immutable record to hold all inspection configuration.
 *
 * @param id1 first text identifier
 * @param id2 second text identifier
 * @param contextSize context size around matches
 * @param displayMinLen minimum length for displaying matches
 *
 * @author ujnaa
 */
public record InspectParameters(String id1, String id2, int contextSize, int displayMinLen) {

    /**
     * Creates InspectParameters with validation.
     *
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @param contextSize context size around matches
     * @param displayMinLen minimum length for displaying matches
     */
    public InspectParameters {
        if (id1 == null || id1.trim().isEmpty()) {
            throw new IllegalArgumentException("First text identifier cannot be null or empty");
        }
        if (id2 == null || id2.trim().isEmpty()) {
            throw new IllegalArgumentException("Second text identifier cannot be null or empty");
        }
        if (contextSize < 0) {
            throw new IllegalArgumentException("Context size must be non-negative");
        }
        if (displayMinLen <= 0) {
            throw new IllegalArgumentException("Display minimum length must be positive");
        }
    }

    /**
     * Creates parameters with default values.
     *
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @return parameters with default context size (0) and display min length (1)
     */
    public static InspectParameters withDefaults(String id1, String id2) {
        return new InspectParameters(id1, id2, 0, 1);
    }

    /**
     * Creates parameters with custom context size but default display min length.
     *
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @param contextSize context size around matches
     * @return parameters with specified context size and default display min length (1)
     */
    public static InspectParameters withContext(String id1, String id2, int contextSize) {
        return new InspectParameters(id1, id2, contextSize, 1);
    }
}