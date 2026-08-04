package me.pauleff.common.handlers.files;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a Minecraft {@code usercache.json} file.
 * <p>
 * Prefer {@link #loadArray(Path)} for one-shot loads. Use {@link #of(Path)} when reusing a handle.
 */
public final class UsercacheFile
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UsercacheFile.class);

    private final Path path;

    private UsercacheFile(Path path)
    {
        this.path = path;
    }

    /**
     * Returns a handle for the given usercache path.
     *
     * @param path the path to {@code usercache.json}
     * @return a usercache file handle
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public static UsercacheFile of(Path path)
    {
        return new UsercacheFile(Objects.requireNonNull(path, "path"));
    }

    /**
     * Loads the contents of the given usercache file as a {@link JSONArray}.
     *
     * @param path the path to {@code usercache.json}
     * @return the parsed JSON array, or an empty array if the file could not be read
     */
    public static JSONArray loadArray(Path path)
    {
        return of(path).loadArray();
    }

    /**
     * Returns the path to this usercache file.
     *
     * @return the usercache file path
     */
    public Path path()
    {
        return path;
    }

    /**
     * Loads the contents of this usercache file as a {@link JSONArray}.
     * <p>
     * On read failure, logs a warning and returns an empty array so callers can continue
     * without prefetched user data.
     *
     * @return the parsed JSON array, or an empty array if the file could not be read
     */
    public JSONArray loadArray()
    {
        try
        {
            String jsonString = Files.readString(path, StandardCharsets.UTF_8);
            LOGGER.debug("Loaded usercache.json from {}", path.normalize());
            return new JSONArray(jsonString);
        } catch (IOException e)
        {
            LOGGER.warn("Could not read usercache.json from path: {}. Continuing without prefetching userdata.",
                    path.normalize());
            return new JSONArray();
        }
    }
}
