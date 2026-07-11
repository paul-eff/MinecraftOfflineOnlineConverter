package me.pauleff.common.exceptions;

import java.nio.file.Path;

public class PathNotValidException extends Exception
{
    private final Path path;

    public PathNotValidException(String explanation, Path path)
    {
        super(String.format("%s: %s", explanation, path));
        this.path = path;
    }

    public PathNotValidException(Path path)
    {
        this("The given path does not exist", path);
    }

    public Path path()
    {
        return path;
    }
}
