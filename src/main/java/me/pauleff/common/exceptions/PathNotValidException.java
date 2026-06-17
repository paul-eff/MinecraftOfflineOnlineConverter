package me.pauleff.common.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PathNotValidException extends Exception
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PathNotValidException.class);

    public PathNotValidException(String explanation, Path path)
    {
        super(String.format("%s: %s", explanation, path));
        LOGGER.error("{}: {}", explanation, path);
    }

    public PathNotValidException(Path path)
    {
        super(String.format("The given path does not exist: %s", path));
        LOGGER.error("The given path does not exist: {}", path);
    }
}
