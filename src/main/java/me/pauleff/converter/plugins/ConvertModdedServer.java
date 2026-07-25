package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

/**
 * Converts world files for a modded Minecraft server (Forge, Fabric, and similar).
 * <p>
 * Targeting and conversion are inherited from {@link AbstractWorldConversionPlugin}.
 */
public class ConvertModdedServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "modded-world",
            "Modded World",
            "Conversion of basic world directories/files from a modded Minecraft server (Forge, Fabric, ...).");

    /**
     * Returns {@link ServerType#MODDED}.
     *
     * @return {@link ServerType#MODDED}
     */
    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.MODDED;
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
