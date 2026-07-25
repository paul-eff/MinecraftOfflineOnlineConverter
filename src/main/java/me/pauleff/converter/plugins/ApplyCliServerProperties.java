package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Applies {@code -properties} key/value pairs from the CLI to {@code server.properties}.
 */
public class ApplyCliServerProperties implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "apply-cli-server-properties",
            "Apply CLI Server Properties",
            "Applies key=value pairs from the -properties CLI option to server.properties.",
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
     * Returns {@code true} when at least one {@code -properties} change was provided.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if there are property changes to apply; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return !ctx.parsedArguments().serverPropertiesChanges().isEmpty();
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
        return List.of(ctx.serverFolder().resolve("server.properties"));
    }

    /**
     * Writes each CLI property change into the resolved {@code server.properties} files.
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
            for (Map.Entry<String, String> entry : ctx.parsedArguments().serverPropertiesChanges().entrySet())
            {
                FileHandler.writeToProperties(path, entry.getKey(), entry.getValue());
            }
        }
    }
}
