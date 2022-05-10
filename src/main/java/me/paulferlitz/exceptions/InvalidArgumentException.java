package me.paulferlitz.exceptions;

/**
 * Exception thrown when wrong or invalid arguments were passed.
 *
 * @author Paul Ferlitz
 */
public class InvalidArgumentException extends Exception
{
    public InvalidArgumentException(String argument)
    {
        super("The argument \"" + argument + "\" was not recognized. Valid arguments are:\n" +
                "-online or -offline for conversion to specified server mode (only one must be specified).\n" +
                "-p \"path/to/server/folder/\" when executing from outside of the server's main folder.");
    }
}
