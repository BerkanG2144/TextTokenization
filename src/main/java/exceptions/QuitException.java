package exceptions;

/**
 * Special CommandException thrown when the quit command is issued from within a special mode
 * (like edit or inspect mode) to signal that the entire application should terminate.
 *
 * @author ujnaa
 */
public class QuitException extends CommandException {

    /**
     * Constructs a new QuitCommandException.
     */
    public QuitException() {
        super("QUIT_REQUESTED");
    }

    /**
     * Checks if this is a quit request.
     * @return true if this represents a quit request
     */
    public boolean isQuitRequest() {
        return "QUIT_REQUESTED".equals(getMessage());
    }
}