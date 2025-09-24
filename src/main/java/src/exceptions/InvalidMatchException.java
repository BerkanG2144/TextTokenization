package src.exceptions;

/**
 * Exception thrown when trying to create or manipulate invalid matches.
 *
 * @author ujnaa
 */
public class InvalidMatchException extends Exception {

    /**
     * Constructs a new InvalidMatchException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidMatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidMatchException for overlapping matches.
     *
     * @param matchDescription description of the match that would cause overlap
     * @return new InvalidMatchException instance
     */
    public static InvalidMatchException forOverlap(String matchDescription) {
        return new InvalidMatchException("Error, " + matchDescription);
    }

    /**
     * Constructs a new InvalidMatchException for out-of-bounds matches.
     *
     * @param position the invalid position
     * @param sequenceLength the length of the sequence
     * @return new InvalidMatchException instance
     */
    public static InvalidMatchException forOutOfBounds(int position, int sequenceLength) {
        return new InvalidMatchException("Match position " + position + " is out of bounds for sequence of length " + sequenceLength);
    }
}
