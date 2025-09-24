package utils;

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
     * @throws IOException if the file cannot be read
     */
    public static String readFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + filePath);
        }

        return Files.readString(path, StandardCharsets.UTF_8);
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

    /**
     * Checks if a file exists and is readable.
     *
     * @param filePath the path to check
     * @return true if the file exists and is readable
     */
    public static boolean isReadableFile(String filePath) {
        if (filePath == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path);
        } catch (Exception e) {
            return false;
        }
    }
}