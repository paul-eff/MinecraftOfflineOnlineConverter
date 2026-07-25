package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Updates {@code online-mode} in {@code server.properties} to match the conversion direction.
 */
public class UpdateProperties implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "update-properties",
            "Update Properties",
            "Sets online-mode from the conversion direction.",
            4);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns {@code true} when an online/offline conversion was requested.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this is a conversion operation; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation();
    }

    /**
     * Returns the {@code server.properties} path under the server folder.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing {@code server.properties}
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(
                ctx.serverFolder().resolve("server.properties"));
    }

    /**
     * Writes {@code online-mode} to {@code true} for online conversion, otherwise {@code false}.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing {@code server.properties} paths to update
     * @throws IOException if writing properties fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        for (Path path : resolvedExistingTargets)
        {
            boolean newPropertyValue = ctx.conversionTarget().equals(ConversionTarget.ONLINE);
            FileHandler.writeToProperties(path, "online-mode", String.valueOf(newPropertyValue));
        }
    }
}
