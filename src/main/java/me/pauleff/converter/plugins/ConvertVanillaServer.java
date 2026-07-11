package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

public class ConvertVanillaServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "vanilla-world",
            "Vanilla World",
            "Conversion of basic world directories/files from a vanilla Minecraft server.");

    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.VANILLA;
    }

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }
}
