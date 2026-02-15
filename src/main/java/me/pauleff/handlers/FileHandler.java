package me.pauleff.handlers;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling file operations efficiently.
 * Provides methods for renaming, listing, reading, and writing files.
 *
 * @author Paul Ferlitz
 */
public class FileHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Renames a file and keep source file extension.
     *
     * @param sourceFile  Current file path.
     * @param newFileName New file name.
     * @throws IOException If renaming is unsuccessful.
     */
    public static void renameFile(Path sourceFile, String newFileName) throws IOException
    {
        Path parentDir = sourceFile.getParent();
        String originalFileName = sourceFile.getFileName().toString();
        String extension = "";

        // Extract the extension from the original file if it exists
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);

            // Avoid double extension if newFileName already has the extension
            if (!newFileName.endsWith(extension)) {
                newFileName = newFileName + extension;
            }
        }

        Path target = parentDir.resolve(newFileName);
        Files.move(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.debug("Renamed file\n\tFROM: '{}'\n\tTO: '{}'", sourceFile.normalize(), target.normalize());
    }

    /**
     * Loads an array of players from a usercache.json file.
     *
     * @param pathToUsercache Path to usercache.json.
     * @return JSONArray containing the user cache data.
     */
    public static JSONArray loadArrayFromUsercache(Path pathToUsercache)
    {
        try
        {
            String jsonString = Files.readString(pathToUsercache, StandardCharsets.UTF_8);
            LOGGER.info("Loaded usercache.json file successfully.");
            return new JSONArray(jsonString);
        } catch (IOException e)
        {
            LOGGER.warn("Could not read usercache.json from path: {}. Continuing without prefetching userdata.", pathToUsercache.normalize());
            return new JSONArray();
        }
    }

    /**
     * Reads the world name from a server.properties file.
     *
     * @param pathToProperties Path to server.properties.
     * @return Extracted world name or default "world" if not found.
     */
    public static String readWorldNameFromProperties(Path pathToProperties)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToProperties.toFile())))
        {
            String worldName = br.lines()
                    .filter(line -> line.startsWith("level-name="))
                    .map(line -> line.substring("level-name=".length()))
                    .findFirst()
                    .orElse("world");
            LOGGER.info("Found world name: '{}'", worldName);
            return worldName;
        } catch (IOException e)
        {
            LOGGER.warn("Could not read server.properties at path: {}. Assuming 'world' to be correct.", pathToProperties.normalize(), e);
            return "world";
        }
    }

    /**
     * Writes or updates a key-value pair in the server.properties file.
     *
     * @param pathToProperties Path to server.properties.
     * @param key              Property key.
     * @param value            Property value.
     */
    public static void writeToProperties(Path pathToProperties, String key, String value)
    {
        try
        {
            List<String> lines = Files.readAllLines(pathToProperties, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .map(line -> line.startsWith(key + "=") ? key + "=" + value : line)
                    .collect(Collectors.toList());
            Files.write(pathToProperties, modifiedLines, StandardCharsets.UTF_8);
            LOGGER.info("Updated property '{}' to value '{}'", key, value);
        } catch (IOException e)
        {
            LOGGER.error("Could not update property '{}' to value '{}' in server.properties at path: {}", key, value, pathToProperties.normalize(), e);
        }
    }

    /**
     * Determines if a file is a text file or a binary file.
     *
     * @param pathToFile Path to the file.
     * @return true if the file is a text file, false otherwise.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If the file is invalid.
     */
    public static boolean isText(Path pathToFile) throws IOException
    {
        File file = pathToFile.toFile();
        if (!file.isFile())
        {
            throw new IllegalArgumentException("Path must be a valid file.");
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            int numberOfNonTextChars = 0;
            while (raf.getFilePointer() < raf.length())
            {
                final int b = raf.readUnsignedByte();
                // http://www.table-ascii.com/
                if (b == 0x09 || // horizontal tabulation
                        b == 0x0A || // line feed
                        b == 0x0C || // form feed
                        b == 0x0D || // carriage return
                        (b >= 0x20 && b <= 0x7E) || // "normal" characters
                        (b >= 0x80 && b <= 0x9F) || // latin-1 symbols
                        (b >= 0xA0 && b <= 0xFF)) // latin-1 symbols
                {
                    // OK
                } else
                {
                    numberOfNonTextChars++;
                }
            }
            boolean isTextFile = numberOfNonTextChars <= 2 && (raf.length() - (double) numberOfNonTextChars / raf.length()) >= 0.99;
            LOGGER.debug("Detected a {} file at {}.{}", isTextFile ? "text" : "binary", pathToFile.normalize(), isTextFile ? "" : " Skipping...");
            return isTextFile;
        }
    }
}
