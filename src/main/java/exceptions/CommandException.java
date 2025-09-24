package exceptions;

/**
 * Exception thrown when a command fails to execute.
 * This exception is used to signal various types of command execution errors,
 * such as invalid arguments, missing resources, or operational failures.
 *
 * @author ujnaa
 */
public class CommandException extends Exception {

    /**
     * Constructs a new CommandException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new CommandException with the specified detail message and cause.
     * This constructor is useful for wrapping lower-level exceptions that caused
     * the command to fail.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of this exception (which is saved for later retrieval
     *              by the {@link #getCause()} method). A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}