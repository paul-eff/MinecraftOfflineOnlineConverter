package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ConvertVanillaServer implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "vanilla-world",
            "Vanilla World",
            "Conversion of basic world directories/files from a vanilla Minecraft server.");

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
        try (Stream<Path> worldFolderStream = Files.walk(ctx.worldFolder()))
        {
            return worldFolderStream
                    .filter(path -> !path.equals(ctx.worldFolder()))
                    .toList();
        } catch (IOException e)
        {
            logger().warn("Could not collect vanilla world targets from {}", ctx.worldFolder().normalize(), e);
            return List.of();
        }
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
