package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.ServerTypePlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

abstract class AbstractWorldConversionPlugin implements ServerTypePlugin
{
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return returnAllFilesInFolders(
                ctx.worldFolderStructure().dimensionRootFolders(ctx.serverFolder(), ctx.worldFolder()));
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        new ConverterV3(ctx).convert(resolvedExistingTargets);
    }
}
