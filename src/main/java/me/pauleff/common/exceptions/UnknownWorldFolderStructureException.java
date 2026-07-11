package me.pauleff.common.exceptions;

public class UnknownWorldFolderStructureException extends RuntimeException
{
    public UnknownWorldFolderStructureException()
    {
        super("Unable to detect this server's world folder structure. Please contact the developer to get this resolved.");
    }
}
