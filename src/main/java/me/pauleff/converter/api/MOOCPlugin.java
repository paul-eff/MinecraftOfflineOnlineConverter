package me.pauleff.converter.api;

import me.pauleff.converter.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Template interface used when creating a custom plugin for MOOC.
 * The {@link PluginMetadata} defines identity and {@linkplain PluginMetadata#priority() execution priority}
 * ({@value PluginMetadata#MIN_PRIORITY}–{@value PluginMetadata#MAX_PRIORITY}, lower runs first). In a
 * {@link PluginRegistry}, ties break by registration order.
 */
public interface MOOCPlugin
{
    default Logger logger()
    {
        return LoggerFactory.getLogger(getClass());
    }

    PluginMetadata metadata();

    /**
     * Paths relative to e.g. {@link PluginContext#serverFolder()} and which will be the target of this plugin's conversion.
     *
     * @param ctx {@link PluginContext} holding information on folders and conversion target.
     * @return {@link List} of {@link Path}s to convert; non-null; may be empty
     */
    List<Path> setTargets(PluginContext ctx);

    /**
     * Do work only on {@code resolvedExistingTargets} (absolute, existing).
     * You may {@linkplain PluginContext#putUuidMapping(java.util.UUID, java.util.UUID) extend} {@link PluginContext#uuidMap()}
     * for later plugins.
     *
     * @param ctx                     {@link PluginContext} holding information on folders and conversion target.
     * @param resolvedExistingTargets {@link List} of {@link Path}s to convert or further work on.
     */
    void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException;

    /**
     * {@code false} skips this plugin for {@code ctx}; default {@code true}.
     */
    default boolean isEnabled(PluginContext ctx)
    {
        return true;
    }

    /**
     * Builds a List of all files to look at and convert dependent on the given root folders to look at.
     *
     * @param worldDimensionRootFolders Root directories ({@link Path}s) filtered out earlier that contain convertable server files.
     * @return {@link List} of {@link Path}s to files to convert.
     */
    default List<Path> returnAllFilesInFolders(List<Path> worldDimensionRootFolders)
    {
        List<Path> targets = new ArrayList<>();
        for (Path rootFolder : worldDimensionRootFolders)
        {
            if (!Files.exists(rootFolder))
            {
                logger().debug("Skipping missing world folder: {}", rootFolder.normalize());
                continue;
            }
            try (Stream<Path> worldFolderStream = Files.walk(rootFolder))
            {
                worldFolderStream
                        .filter(path -> !path.equals(rootFolder))
                        .forEach(targets::add);
            } catch (IOException e)
            {
                logger().warn("Could not collect world targets from {}", rootFolder.normalize(), e);
            }
        }
        return targets;
    }
}
