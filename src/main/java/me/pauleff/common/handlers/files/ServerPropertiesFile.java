package me.pauleff.common.handlers.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a Minecraft {@code server.properties} file and supports order-preserving edits.
 * <p>
 * Prefer the static one-shot methods for single operations. Use {@link #of(Path)} when performing
 * multiple edits on the same file.
 */
public final class ServerPropertiesFile
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPropertiesFile.class);
    private static final String LEVEL_NAME_KEY = "level-name";
    private static final String DEFAULT_WORLD_NAME = "world";

    private final Path path;

    private ServerPropertiesFile(Path path)
    {
        this.path = path;
    }

    /**
     * Returns a handle for the given properties file path.
     *
     * @param path the path to {@code server.properties}
     * @return a properties file handle
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public static ServerPropertiesFile of(Path path)
    {
        return new ServerPropertiesFile(Objects.requireNonNull(path, "path"));
    }

    /**
     * Reads the {@code level-name} value from the given properties file.
     *
     * @param path the path to {@code server.properties}
     * @return the configured world name, or {@code "world"} as a fallback
     */
    public static String worldName(Path path)
    {
        return of(path).worldName();
    }

    /**
     * Updates a key-value entry in the given properties file, or appends it if absent.
     *
     * @param path  the path to the properties file
     * @param key   the property key to set
     * @param value the property value to write
     * @throws IOException if reading or writing the file fails
     */
    public static void writeProperty(Path path, String key, String value) throws IOException
    {
        of(path).writeProperty(key, value);
    }

    /**
     * Returns the path to this properties file.
     *
     * @return the properties file path
     */
    public Path path()
    {
        return path;
    }

    /**
     * Reads the {@code level-name} value from this properties file.
     * <p>
     * Returns {@code "world"} if the property is missing or the file cannot be read.
     *
     * @return the configured world name, or {@code "world"} as a fallback
     */
    public String worldName()
    {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8))
        {
            String worldName = lines
                    .filter(line -> line.startsWith(LEVEL_NAME_KEY + "="))
                    .map(line -> line.substring((LEVEL_NAME_KEY + "=").length()))
                    .findFirst()
                    .orElse(DEFAULT_WORLD_NAME);
            LOGGER.debug("Found world name: '{}'", worldName);
            return worldName;
        } catch (IOException e)
        {
            LOGGER.warn("Could not read server.properties at path: {}. Assuming '{}' to be correct.",
                    path.normalize(), DEFAULT_WORLD_NAME, e);
            return DEFAULT_WORLD_NAME;
        }
    }

    /**
     * Updates a key-value entry in this properties file, or appends it if the key is not present.
     * <p>
     * Matching lines are identified by a {@code key=} prefix. Line order and unrelated content
     * are preserved. The file is rewritten in place.
     *
     * @param key   the property key to set
     * @param value the property value to write
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     * @throws IOException          if reading or writing the file fails
     */
    public void writeProperty(String key, String value) throws IOException
    {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        List<String> lines = readLines();
        List<String> updated = replaceOrAppend(lines, key, value);
        writeLines(updated);
        LOGGER.info("Updated property '{}' to value '{}'", key, value);
    }

    private List<String> readLines() throws IOException
    {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    private static List<String> replaceOrAppend(List<String> lines, String key, String value)
    {
        String prefix = key + "=";
        String entry = prefix + value;
        List<String> modified = new ArrayList<>(lines.size() + 1);
        boolean found = false;
        for (String line : lines)
        {
            if (line.startsWith(prefix))
            {
                modified.add(entry);
                found = true;
            } else
            {
                modified.add(line);
            }
        }
        if (!found)
        {
            modified.add(entry);
        }
        return modified;
    }

    private void writeLines(List<String> lines) throws IOException
    {
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
