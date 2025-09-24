package exceptions;

import java.io.IOException;

/**
 * Exception thrown when a file operation fails.
 * This exception wraps IOException and provides more context
 * about the specific file operation that failed.
 *
 * @author ujnaa
 */
public class FileOperationException extends Exception {

    private final String fileName;
    private final String operation;

    /**
     * Constructs a new FileOperationException with the specified details.
     *
     * @param fileName the name of the file that caused the error
     * @param operation the operation that was being performed
     * @param cause the underlying IOException
     */
    public FileOperationException(String fileName, String operation, IOException cause) {
        super("Failed to " + operation + " file '" + fileName + "': " + cause.getMessage(), cause);
        this.fileName = fileName;
        this.operation = operation;
    }

    /**
     * Constructs a new FileOperationException with a custom message.
     *
     * @param fileName the name of the file that caused the error
     * @param operation the operation that was being performed
     * @param message the detail message
     */
    public FileOperationException(String fileName, String operation, String message) {
        super("Failed to " + operation + " file '" + fileName + "': " + message);
        this.fileName = fileName;
        this.operation = operation;
    }

    /**
     * Gets the name of the file that caused the error.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the operation that was being performed.
     *
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }
}