package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

public class ConvertModdedServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "modded-world",
            "Modded World",
            "Conversion of basic world directories/files from a modded Minecraft server (Forge, Fabric, ...).");

    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.MODDED;
    }

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }
}
