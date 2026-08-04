package me.pauleff.common.handlers.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Renames files while optionally preserving the source file extension.
 */
public final class FileRenamer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRenamer.class);

    private FileRenamer()
    {
    }

    /**
     * Renames a file, preserving the original extension when the new name does not include one.
     * <p>
     * If the source has an extension and {@code newFileName} does not end with that extension,
     * the extension is appended. An existing file at the target path is replaced.
     *
     * @param source      the file to rename
     * @param newFileName the desired file name, with or without an extension
     * @return the path of the renamed file
     * @throws NullPointerException if {@code source} or {@code newFileName} is {@code null}
     * @throws IOException          if the rename fails
     */
    public static Path renamePreservingExtension(Path source, String newFileName) throws IOException
    {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(newFileName, "newFileName");

        String resolvedName = preserveExtension(source.getFileName().toString(), newFileName);
        Path target = source.getParent().resolve(resolvedName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.debug("Renamed file\n\tFROM: '{}'\n\tTO: '{}'", source.normalize(), target.normalize());
        return target;
    }

    /**
     * Appends the original file extension to {@code newFileName} when it is missing.
     *
     * @param originalFileName the current file name, possibly with an extension
     * @param newFileName      the desired file name
     * @return {@code newFileName} with the original extension preserved when applicable
     */
    private static String preserveExtension(String originalFileName, String newFileName)
    {
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex <= 0)
        {
            return newFileName;
        }
        String extension = originalFileName.substring(dotIndex);
        if (newFileName.endsWith(extension))
        {
            return newFileName;
        }
        return newFileName + extension;
    }
}
