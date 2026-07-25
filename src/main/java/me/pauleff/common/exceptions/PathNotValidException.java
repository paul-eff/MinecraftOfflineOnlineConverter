package me.pauleff.common.exceptions;

import java.nio.file.Path;

/**
 * Indicates that a filesystem path is missing or otherwise unsuitable for use.
 */
public class PathNotValidException extends Exception
{
    private final Path path;

    /**
     * Creates an exception for the given path with a custom explanation.
     * <p>
     * The detail message is formatted as {@code explanation: path}.
     *
     * @param explanation a short description of why the path is invalid
     * @param path        the invalid path
     */
    public PathNotValidException(String explanation, Path path)
    {
        super(String.format("%s: %s", explanation, path));
        this.path = path;
    }

    /**
     * Creates an exception for a path that does not exist.
     *
     * @param path the missing path
     */
    public PathNotValidException(Path path)
    {
        this("The given path does not exist", path);
    }

    /**
     * Returns the path that caused this exception.
     *
     * @return the invalid path
     */
    public Path path()
    {
        return path;
    }
}
