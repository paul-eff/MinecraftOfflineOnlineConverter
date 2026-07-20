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
        ctx.setSaveFileFormat(hasAnvilRegionFiles(ctx) ? ANVIL : MC_REGION);
        logger().info("Detected save file format: {}", ctx.saveFileFormat().name());

        /*
         * During a normal conversion run this is the first point where we can determine if a conversion is even needed.
         * Minecraft UUIDs where firstly used in Minecraft 1.7.6 (2014) alongside Mojang account migration support and name changing.
         * The easiest way to determine this is via the file format not being ANVIL.
         */
        if (ctx.saveFileFormat() == MC_REGION)
        {
            logger().info("You are probably trying to convert a Minecraft older then Minecraft 1.7.6 (2014). Switching between online and offline should work without any conversion needed!");
            System.exit(0);
        }
    }

    private boolean hasAnvilRegionFiles(PluginContext ctx) throws IOException
    {
        try (Stream<Path> pathStream = Files.walk(ctx.worldFolder()))
        {
            return pathStream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.endsWith(".mca"));
        }
    }
}
