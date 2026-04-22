package me.pauleff.converter.plugins;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class DetectWorldFormat implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-world-format",
            "Detect World Format",
            "Detect the Minecraft world format (Alpha, McRegion, Anvil...).",
            1);

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
        return List.of();
    }

    /**
     * Do work only on {@code resolvedExistingTargets} (absolute, existing).
     * You may {@linkplain PluginContext#putUuidMapping(UUID, UUID) extend} {@link PluginContext#uuidMap()}
     * for later plugins.
     *
     * @param ctx                     {@link PluginContext} holding information on folders and conversion target.
     * @param resolvedExistingTargets {@link List} of {@link Path}s to convert or further work on.
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {

    }
}
