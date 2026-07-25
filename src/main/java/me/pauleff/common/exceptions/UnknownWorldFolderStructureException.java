package me.pauleff.common.exceptions;

/**
 * Indicates that the world's folder layout could not be recognized.
 */
public class UnknownWorldFolderStructureException extends RuntimeException
{
    /**
     * Creates an exception with a fixed message asking the user to contact the developer.
     */
    public UnknownWorldFolderStructureException()
    {
        super("Unable to detect this server's world folder structure. Please contact the developer to get this resolved.");
    }
}
