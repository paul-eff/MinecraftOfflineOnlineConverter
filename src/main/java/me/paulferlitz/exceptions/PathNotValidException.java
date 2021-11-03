package me.paulferlitz.exceptions;

public class PathNotValidException extends Exception
{
    public PathNotValidException(String path)
    {
        super("The given path does not exists!\nPath in question: " + path);
    }
}
