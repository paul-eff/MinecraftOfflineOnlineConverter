package me.paulferlitz.exceptions;

public class InvalidArgumentException extends Exception
{
    public InvalidArgumentException(String argument)
    {
        super("The argument \"" + argument + "\" was not recognized. Valid arguments are:\n" +
                "-online or -offline for conversion to specified server mode (only one must be specified).\n" +
                "-p \"path/to/world/folder/\" when executing from outside of world folder or the world has a custom name");
    }
}
