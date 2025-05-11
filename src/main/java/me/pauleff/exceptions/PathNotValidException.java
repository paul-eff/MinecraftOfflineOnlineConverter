package me.pauleff.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Exception thrown when a given file path is invalid or does not exist.
 * Logs the invalid path for better debugging.
 *
 * @author Paul Ferlitz
 */
public class PathNotValidException extends Exception
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PathNotValidException.class);

    /**
     * Constructs a PathNotValidException with a detailed message.
     *
     * @param path The invalid path that caused the exception.
     */
    public PathNotValidException(Path path)
    {
        super(String.format("The given path does not exist: %s", path));
        LOGGER.error("The given path does not exist: {}", path);
    }
}
