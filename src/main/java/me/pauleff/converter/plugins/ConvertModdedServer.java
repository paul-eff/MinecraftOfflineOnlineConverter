package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.WorldFolderStructure;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConvertModdedServer implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "modded-world",
            "Modded World",
            "Conversion of basic world directories/files from a modded Minecraft server (Forge, Fabric, ...).");

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Paths relative to e.g. {@link PluginContext#serverFolder()} and which will be the target of this plugin's conversion.
     *
     * @param ctx {@link PluginContext} holding information on folders and conversion target.
     * @return {@link List} of {@link Path}s to convert; non-null; may be empty
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        List<Path> worldDimensionRootFolders = new ArrayList<>();
        worldDimensionRootFolders.add(ctx.worldFolder());

        if (ctx.worldFolderStructure() == WorldFolderStructure.PER_DIMENSION)
        {
            Path worldName = ctx.worldFolder().getFileName();
            worldDimensionRootFolders.add(ctx.serverFolder().resolve(worldName + "_nether"));
            worldDimensionRootFolders.add(ctx.serverFolder().resolve(worldName + "_the_end"));
        }

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
                logger().warn("Could not collect Bukkit world targets from {}", rootFolder.normalize(), e);
            }
        }
        return targets;
    }

    /**
     * Do work only on {@code resolvedExistingTargets} (absolute, existing).
     *
     * @param ctx                     {@link PluginContext} holding information on folders and conversion target.
     * @param resolvedExistingTargets {@link List} of {@link Path}s to convert or further work on.
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        ConverterV3 converterV3 = new ConverterV3(ctx);
        converterV3.convert(resolvedExistingTargets);
    }
}
