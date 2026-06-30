package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class UpdateProperties implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "update-properties",
            "Update Properties",
            "Sets online-mode from the conversion direction.",
            4);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(
                ctx.serverFolder().resolve("server.properties"));
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        resolvedExistingTargets.forEach(path -> {
            boolean newPropertyValue = ctx.conversionTarget().equals(ConversionTarget.ONLINE);
            FileHandler.writeToProperties(path, "online-mode", String.valueOf(newPropertyValue));
        });
    }
}
