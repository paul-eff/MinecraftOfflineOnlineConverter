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

/**
 * Detects the world's region save format (Anvil vs McRegion) and stores it on the
 * {@link PluginContext}.
 * <p>
 * If McRegion is detected, the process exits early because UUID-based conversion is
 * not needed for pre-1.7.6 worlds.
 */
public class DetectSaveFileFormat implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-world-savefile-format",
            "Detect World save file format",
            "Detect the Minecraft's world file format (Alpha, McRegion, Anvil...).",
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
     * Returns the world folder as the sole detection target.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the world folder
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.worldFolder());
    }

    /**
     * Sets the save file format on the context, or exits if McRegion (pre-UUID) is found.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets unused; detection walks the world folder from {@code ctx}
     * @throws IOException if walking the world folder fails
     */
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

    /**
     * Indicates whether any Anvil ({@code .mca}) region files exist under the world folder.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if at least one {@code .mca} file is found; {@code false} otherwise
     * @throws IOException if walking the world folder fails
     */
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
