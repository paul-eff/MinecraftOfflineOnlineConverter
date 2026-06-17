package me.pauleff.converter.plugins;

import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ApplyCliServerProperties implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "apply-cli-server-properties",
            "Apply CLI Server Properties",
            "Applies key=value pairs from the -properties CLI option to server.properties.",
            1);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        ParsedArguments parsedArguments = ctx.parsedArguments();
        return parsedArguments != null && !parsedArguments.serverPropertiesChanges().isEmpty();
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder().resolve("server.properties"));
    }

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
