package me.pauleff.handlers;

import me.pauleff.Main;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling file operations efficiently.
 * Provides methods for renaming, listing, reading, and writing files.
 *
 * @author Paul Ferlitz
 */
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Renames a file within the specified base directory.
     *
     * @param baseWorldFolder Base directory.
     * @param oldFilePath     Current file path relative to base.
     * @param newFilePath     New file path relative to base.
     * @throws IOException If renaming is unsuccessful.
     */
    public static void renameFile(String baseWorldFolder, String oldFilePath, String newFilePath) throws IOException {
        Path source = Paths.get(baseWorldFolder, oldFilePath);
        Path target = Paths.get(baseWorldFolder, newFilePath);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        if (Main.getArgs().hasOption("v")) logger.info("Renamed file from '{}' to '{}'", source, target);
    }

    /**
     * Lists all files in the specified directory.
     *
     * @param worldFolderPath Path to the directory.
     * @return Array of {@link File} objects representing the files in the directory.
     */
    public static File[] listAllFiles(String worldFolderPath) {
        File folder = new File(worldFolderPath);
        if (!folder.isDirectory()) {
            logger.warn("Invalid directory path: {}. Trying to resolve to file.", worldFolderPath);
            return new File[0];
        }
        return folder.listFiles();
    }

    /**
     * Loads an array of players from a usercache.json file.
     *
     * @param pathToUserCache Path to usercache.json.
     * @return JSONArray containing the user cache data.
     */
    public static JSONArray loadArrayFromUsercache(String pathToUserCache) {
        try {
            String jsonString = Files.readString(Path.of(pathToUserCache), StandardCharsets.UTF_8);
            logger.info("Loaded usercache from {}", pathToUserCache);
            return new JSONArray(jsonString);
        } catch (IOException e) {
            logger.warn("Could not read usercache.json from path: {}. Continuing without prefetching userdata.", pathToUserCache);
            return new JSONArray();
        }
    }

    /**
     * Reads the world name from a server.properties file.
     *
     * @param pathToProperties Path to server.properties.
     * @return Extracted world name or default "world" if not found.
     */
    public static String readWorldNameFromProperties(String pathToProperties) {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToProperties))) {
            String worldName = br.lines()
                    .filter(line -> line.startsWith("level-name="))
                    .map(line -> line.substring("level-name=".length()))
                    .findFirst()
                    .orElse("world");
            logger.info("Found world name: '{}'", worldName);
            return worldName;
        } catch (IOException e) {
            logger.warn("Could not read server.properties at path: {}. Assuming 'world' to be correct.", pathToProperties, e);
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
    public static void writeToProperties(Path pathToProperties, String key, String value) {
        try {
            List<String> lines = Files.readAllLines(pathToProperties, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .map(line -> line.startsWith(key + "=") ? key + "=" + value : line)
                    .collect(Collectors.toList());
            Files.write(pathToProperties, modifiedLines, StandardCharsets.UTF_8);
            if (Main.getArgs().hasOption("v")) logger.info("Updated property '{}' to value '{}' in {}", key, value, pathToProperties);
        } catch (IOException e) {
            logger.error("Could not update property '{}' to value '{}' in server.properties at {}", key, value, pathToProperties, e);
        }
    }

    /**
     * Determines if a file is a text file or a binary file.
     *
     * @param path Path to the file.
     * @return true if the file is a text file, false otherwise.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If the file is invalid.
     */
    public static boolean isText(final Path path) throws IOException {
        File file = path.toFile();
        if (!file.isFile()) {
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
            if (Main.getArgs().hasOption("v")) {
                if (Main.getArgs().hasOption("v")) logger.info("Detected a {} file at {}.{}", isTextFile ? "text" : "binary", path, isTextFile ? "" : " Skipping...");
            }
            return isTextFile;
        }
    }
}
