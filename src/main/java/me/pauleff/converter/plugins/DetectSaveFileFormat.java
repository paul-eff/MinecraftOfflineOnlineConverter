package me.pauleff.converter.plugins;

import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static me.pauleff.converter.SaveFileFormat.ANVIL;
import static me.pauleff.converter.SaveFileFormat.MC_REGION;

public class DetectSaveFileFormat implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-world-savefile-format",
            "Detect World save file format",
            "Detect the Minecraft's world file format (Alpha, McRegion, Anvil...).",
            1);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.worldFolder());
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        if (isMCA(ctx))
        {
            ctx.setSaveFileFormat(ANVIL);
        } else
        {
            ctx.setSaveFileFormat(MC_REGION);
        }
        logger().info("Detected save file format: {}", ctx.worldSaveFileFormat().name());
    }

    private boolean isMCR(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.walk(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.endsWith(".mcr"));
        }
    }

    private boolean isMCA(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.walk(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.endsWith(".mca"));
        }
    }
}
