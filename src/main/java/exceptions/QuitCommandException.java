package exceptions;

/**
 * Special CommandException thrown when the quit command is issued from within a special mode
 * (like edit or inspect mode) to signal that the entire application should terminate.
 *
 * @author ujnaa
 */
public class QuitCommandException extends CommandException {

    /**
     * Constructs a new QuitCommandException with no message.
     */
    public QuitCommandException() {
        super("");
    }

    /**
     * Checks if this is a quit request.
     * @return true (always, as this exception only represents quit requests)
     */
    public boolean isQuitRequest() {
        return true;
    }
}