package me.pauleff.converter.plugins;

import me.pauleff.common.exceptions.UnknownWorldFolderStructureException;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static me.pauleff.converter.WorldFolderStructure.*;

/**
 * Detects how dimension folders are laid out relative to the world and stores the
 * result on the {@link PluginContext}.
 * <p>
 * Distinguishes a single world folder (classic {@code DIM*} or 2026-style
 * {@code dimensions/minecraft}), per-dimension sibling folders, and unknown layouts.
 */
public class DetectWorldFolderStructure implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-world-format",
            "Detect World Format",
            "Detect the Minecraft world format (single folder, _ separated dimension folders, single folder 2026.1 style).",
            1);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns the server root folder as the sole detection target.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the server folder
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder());
    }

    /**
     * Sets the world folder structure on the context from the server and world layout.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets unused; detection inspects folders from {@code ctx}
     * @throws IOException                          if listing server or world directories fails
     * @throws UnknownWorldFolderStructureException if a single world folder has no recognized dimension layout
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        if (hasSingleWorldFolder(ctx))
        {
            if (hasDIMFoldersInWorldFolder(ctx))
            {
                ctx.setWorldFolderStructure(SINGLE);
            } else if (hasMinecraftDimensionsFolderInWorldFolder(ctx))
            {
                ctx.setWorldFolderStructure(SINGLE_2026);
            } else
            {
                throw new UnknownWorldFolderStructureException();
            }
        } else
        {
            ctx.setWorldFolderStructure(PER_DIMENSION);
        }
        logger().info("Detected world folder structure: {}", ctx.worldFolderStructure().name());
    }

    /**
     * Indicates whether the server root has no sibling {@code *_nether} / {@code *_the_end} folders.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if dimensions appear to live under a single world folder
     * @throws IOException if listing the server folder fails
     */
    private boolean hasSingleWorldFolder(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.list(ctx.serverFolder()))
        {
            return pathStream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .noneMatch(name -> name.contains("_nether") || name.contains("_the_end"));
        }
    }

    /**
     * Indicates whether the world folder contains classic {@code DIM1} / {@code DIM-1} directories.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if classic dimension folders are present
     * @throws IOException if listing the world folder fails
     */
    private boolean hasDIMFoldersInWorldFolder(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.list(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.equals("dim1") || name.equals("dim-1"));
        }
    }

    /**
     * Indicates whether the world folder contains a {@code dimensions/minecraft} hierarchy.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if the 2026-style dimensions layout is present
     * @throws IOException if listing the world folder fails
     */
    private boolean hasMinecraftDimensionsFolderInWorldFolder(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.list(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("dimensions"))
                    .anyMatch(dimensionsPath -> {
                        try (Stream<Path> dimensionsContents = Files.list(dimensionsPath))
                        {
                            return dimensionsContents.filter(Files::isDirectory)
                                    .anyMatch(path -> path.getFileName().toString().equalsIgnoreCase("minecraft"));
                        } catch (IOException e)
                        {
                            return false;
                        }
                    });
        }
    }
}
