package me.pauleff.exceptions;

/**
 * Exception thrown when a given file path is invalid or does not exist.
 * Logs the invalid path for better debugging.
 *
 * @author Paul Ferlitz
 */
public class PathNotValidException extends Exception {
    /**
     * Constructs a PathNotValidException with a detailed message.
     *
     * @param path The invalid path that caused the exception.
     */
    public PathNotValidException(String path) {
        super(String.format("The given path does not exist! Path in question: %s", path));
    }
}
