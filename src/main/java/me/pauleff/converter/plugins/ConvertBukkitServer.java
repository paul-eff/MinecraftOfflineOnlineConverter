package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

public class ConvertBukkitServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "bukkit-world",
            "Bukkit World",
            "Conversion of basic world directories/files from a Bukkit Minecraft server (Bukkit, Paper, ...).");

    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.BUKKIT;
    }

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }
}
