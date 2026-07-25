package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

/**
 * Converts world files for a Bukkit-based Minecraft server (Bukkit, Spigot, Paper, and similar).
 * <p>
 * Targeting and conversion are inherited from {@link AbstractWorldConversionPlugin}.
 */
public class ConvertBukkitServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "bukkit-world",
            "Bukkit World",
            "Conversion of basic world directories/files from a Bukkit Minecraft server (Bukkit, Paper, ...).");

    /**
     * Returns {@link ServerType#BUKKIT}.
     *
     * @return {@link ServerType#BUKKIT}
     */
    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.BUKKIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }
}
