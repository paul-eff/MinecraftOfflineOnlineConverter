package me.pauleff.common.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnknownWorldFolderStructureException extends RuntimeException
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownWorldFolderStructureException.class);

    public UnknownWorldFolderStructureException()
    {
        super("Unknown world folder structure detected");
        LOGGER.error("MOOC was unable to detect this server's world folder structure. Please contact the developer to get this resolved!");
    }
}
