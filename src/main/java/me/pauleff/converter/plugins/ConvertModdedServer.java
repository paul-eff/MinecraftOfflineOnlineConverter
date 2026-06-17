package me.pauleff.converter.plugins;

import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.api.ModdedServerPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ConvertModdedServer implements ModdedServerPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "modded-world",
            "Modded World",
            "Conversion of basic world directories/files from a modded Minecraft server (Forge, Fabric, ...).");

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        List<Path> worldDimensionRootFolders = ctx.worldFolderStructure().dimensionRootFolders(ctx.serverFolder(), ctx.worldFolder());
        return returnAllFilesInFolders(worldDimensionRootFolders);
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        ConverterV3 converterV3 = new ConverterV3(ctx);
        converterV3.convert(resolvedExistingTargets);
    }
}
