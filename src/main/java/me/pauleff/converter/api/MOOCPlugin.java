package me.pauleff.converter.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines a conversion plugin that can inspect and modify Minecraft server or world files.
 * <p>
 * Implementations declare metadata, decide when they apply, select target paths, and
 * perform their work against a shared {@link PluginContext}. Specialized sealed subtypes
 * control enablement for default, single-server, and multi-server plugins.
 *
 * @see DefaultPlugin
 * @see ServerTypePlugin
 * @see MultiServerPlugin
 * @see PluginContext
 * @see PluginMetadata
 */
public sealed interface MOOCPlugin permits DefaultPlugin, MultiServerPlugin, ServerTypePlugin
{
    /**
     * Returns a logger named after the runtime class of this plugin.
     *
     * @return a non-{@code null} SLF4J logger for this plugin instance
     */
    default Logger logger()
    {
        return LoggerFactory.getLogger(getClass());
    }

    /**
     * Returns the metadata that identifies and describes this plugin.
     *
     * @return the plugin metadata
     */
    PluginMetadata metadata();

    /**
     * Determines the candidate file or directory paths this plugin may operate on.
     * <p>
     * Paths need not all exist yet; callers typically resolve and filter them before
     * invoking {@link #run(PluginContext, List)}.
     *
     * @param ctx the shared conversion context
     * @return the list of target paths for this plugin; never {@code null}
     */
    List<Path> setTargets(PluginContext ctx);

    /**
     * Indicates whether this plugin should run for the given context.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if the plugin is applicable; {@code false} otherwise
     */
    boolean isEnabled(PluginContext ctx);

    /**
     * Executes this plugin's conversion or inspection logic.
     * <p>
     * Receives only targets that were declared by {@link #setTargets(PluginContext)}
     * and confirmed to exist by the caller.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing target paths to process
     * @throws IOException if reading or writing server or world files fails
     */
    void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException;

    /**
     * Collects all regular files under the given world dimension root folders.
     * <p>
     * Missing roots are skipped with a debug log. Walk failures for a root are logged
     * as warnings and do not abort collection from other roots. The root directories
     * themselves are excluded from the result.
     *
     * @param worldDimensionRootFolders the world or dimension root directories to walk
     * @return all regular files found beneath the existing roots; never {@code null}
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
                        .filter(Files::isRegularFile)
                        .forEach(targets::add);
            } catch (IOException e)
            {
                logger().warn("Could not collect world targets from {}", rootFolder.normalize(), e);
            }
        }
        return targets;
    }
}
