package src.utils;

import exceptions.FileOperationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for file operations.
 *
 * @author ujnaa
 */
public final class FileUtils {

    private FileUtils() {
        //
    }

    /**
     * Reads the content of a file as UTF-8 encoded text.
     *
     * @param filePath the path to the file
     * @return the file content as string
     * @throws FileOperationException if the file cannot be read
     */
    public static String readFile(String filePath) throws FileOperationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new FileOperationException("<unknown>", "read", "File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new FileOperationException(filePath, "read", "File does not exist");
        }

        if (!Files.isRegularFile(path)) {
            throw new FileOperationException(filePath, "read", "Path is not a regular file");
        }

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileOperationException(filePath, "read", e);
        }
    }


    /**
     * Extracts the filename from a file path.
     *
     * @param filePath the full file path
     * @return the filename without directory path
     */
    public static String extractFilename(String filePath) {
        if (filePath == null) {
            return null;
        }

        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
}
