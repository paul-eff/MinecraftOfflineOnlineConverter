package me.paulferlitz.exceptions;

/**
 * Exception when a given file path could not be resolved.
 */
public class PathNotValidException extends Exception
{
    public PathNotValidException(String path)
    {
        super("The given path does not exists!\nPath in question: " + path);
    }
}
