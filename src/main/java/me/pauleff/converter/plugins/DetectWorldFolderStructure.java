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

public class DetectWorldFolderStructure implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-world-format",
            "Detect World Format",
            "Detect the Minecraft world format (single folder, _ separated dimension folders, single folder 2026.1 style).",
            1);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder());
    }

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

    private boolean hasSingleWorldFolder(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.list(ctx.serverFolder()))
        {
            return pathStream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .noneMatch(name -> name.contains("_nether") || name.contains("_the_end"));
        }
    }

    private boolean hasDIMFoldersInWorldFolder(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.list(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.equals("DIM1") || name.equals("DIM-1"));
        }
    }

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
