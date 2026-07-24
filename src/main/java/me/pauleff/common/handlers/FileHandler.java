package me.pauleff.common.handlers;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities for reading, writing, renaming, and inspecting Minecraft server files.
 */
public final class FileHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    private FileHandler()
    {
    }

    /**
     * Renames a file, preserving the original extension when the new name does not include one.
     * <p>
     * If the source has an extension and {@code newFileName} does not end with that extension,
     * the extension is appended. An existing file at the target path is replaced.
     *
     * @param sourceFile  the file to rename
     * @param newFileName the desired file name, with or without an extension
     * @throws IOException if the rename fails
     */
    public static void renameFile(Path sourceFile, String newFileName) throws IOException
    {
        Path parentDir = sourceFile.getParent();
        String originalFileName = sourceFile.getFileName().toString();
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0)
        {
            String extension = originalFileName.substring(dotIndex);
            if (!newFileName.endsWith(extension))
            {
                newFileName = newFileName + extension;
            }
        }

        Path target = parentDir.resolve(newFileName);
        Files.move(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.debug("Renamed file\n\tFROM: '{}'\n\tTO: '{}'", sourceFile.normalize(), target.normalize());
    }

    /**
     * Loads the contents of a {@code usercache.json} file as a {@link JSONArray}.
     * <p>
     * On read failure, logs a warning and returns an empty array so callers can continue
     * without prefetched user data.
     *
     * @param pathToUsercache the path to {@code usercache.json}
     * @return the parsed JSON array, or an empty array if the file could not be read
     */
    public static JSONArray loadArrayFromUsercache(Path pathToUsercache)
    {
        try
        {
            String jsonString = Files.readString(pathToUsercache, StandardCharsets.UTF_8);
            LOGGER.debug("Loaded usercache.json from {}", pathToUsercache.normalize());
            return new JSONArray(jsonString);
        } catch (IOException e)
        {
            LOGGER.warn("Could not read usercache.json from path: {}. Continuing without prefetching userdata.", pathToUsercache.normalize());
            return new JSONArray();
        }
    }

    /**
     * Reads the {@code level-name} value from a {@code server.properties} file.
     * <p>
     * Returns {@code "world"} if the property is missing or the file cannot be read.
     *
     * @param pathToProperties the path to {@code server.properties}
     * @return the configured world name, or {@code "world"} as a fallback
     */
    public static String readWorldNameFromProperties(Path pathToProperties)
    {
        try
        {
            String worldName = Files.lines(pathToProperties, StandardCharsets.UTF_8)
                    .filter(line -> line.startsWith("level-name="))
                    .map(line -> line.substring("level-name=".length()))
                    .findFirst()
                    .orElse("world");
            LOGGER.debug("Found world name: '{}'", worldName);
            return worldName;
        } catch (IOException e)
        {
            LOGGER.warn("Could not read server.properties at path: {}. Assuming 'world' to be correct.", pathToProperties.normalize(), e);
            return "world";
        }
    }

    /**
     * Updates a key-value entry in a properties file, or appends it if the key is not present.
     * <p>
     * Matching lines are identified by a {@code key=} prefix. The file is rewritten in place.
     *
     * @param pathToProperties the path to the properties file
     * @param key              the property key to set
     * @param value            the property value to write
     * @throws IOException if reading or writing the file fails
     */
    public static void writeToProperties(Path pathToProperties, String key, String value) throws IOException
    {
        List<String> lines = Files.readAllLines(pathToProperties, StandardCharsets.UTF_8);
        List<String> modifiedLines = new ArrayList<>(lines.size() + 1);
        boolean found = false;
        for (String line : lines)
        {
            if (line.startsWith(key + "="))
            {
                modifiedLines.add(key + "=" + value);
                found = true;
            } else
            {
                modifiedLines.add(line);
            }
        }
        if (!found)
        {
            modifiedLines.add(key + "=" + value);
        }
        Files.write(pathToProperties, modifiedLines, StandardCharsets.UTF_8);
        LOGGER.info("Updated property '{}' to value '{}'", key, value);
    }

    /**
     * Determines whether a file appears to be text-based rather than binary.
     * <p>
     * Scans byte values against printable ASCII, Latin-1, and common control characters,
     * then applies a heuristic on the ratio of non-text bytes.
     *
     * @param pathToFile the path to the file to inspect
     * @return {@code true} if the file is classified as text-based; {@code false} otherwise
     * @throws IllegalArgumentException if {@code pathToFile} is not a regular file
     * @throws IOException              if the file cannot be read
     */
    public static boolean isTextBasedFile(Path pathToFile) throws IOException
    {
        if (!Files.isRegularFile(pathToFile))
        {
            throw new IllegalArgumentException("Path must be a valid file.");
        }
        try (RandomAccessFile raf = new RandomAccessFile(pathToFile.toFile(), "r"))
        {
            int numberOfNonTextChars = 0;
            while (raf.getFilePointer() < raf.length())
            {
                final int b = raf.readUnsignedByte();
                // http://www.table-ascii.com/
                if (!(b == 0x09 || // horizontal tabulation
                        b == 0x0A || // line feed
                        b == 0x0C || // form feed
                        b == 0x0D || // carriage return
                        (b >= 0x20 && b <= 0x7E) || // "normal" characters
                        (b >= 0x80 && b <= 0x9F) || // latin-1 symbols
                        (b >= 0xA0 && b <= 0xFF))) // latin-1 symbols
                {
                    numberOfNonTextChars++;
                }
            }
            boolean isTextFile = numberOfNonTextChars <= 2 && (raf.length() - (double) numberOfNonTextChars / raf.length()) >= 0.99;
            LOGGER.debug("Detected a {} file at {}.{}", isTextFile ? "text" : "binary", pathToFile.normalize(), isTextFile ? "" : " Skipping...");
            return isTextFile;
        }
    }

    /**
     * Strips the file extension from a file name.
     * <p>
     * Uses the last {@code '.'} as the extension separator. Names without a qualifying
     * extension are returned unchanged (aside from trimming around a found extension).
     *
     * @param fileName the file name to process
     * @return the file name without its extension, or {@code fileName} if no extension is found
     */
    public static String stripFileExtension(String fileName)
    {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex).trim() : fileName;
    }
}
