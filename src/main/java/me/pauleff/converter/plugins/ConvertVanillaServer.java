package me.pauleff.converter.plugins;

import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.PluginMetadata;

/**
 * Converts world files for a Vanilla Minecraft server.
 * <p>
 * Targeting and conversion are inherited from {@link AbstractWorldConversionPlugin}.
 */
public class ConvertVanillaServer extends AbstractWorldConversionPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "vanilla-world",
            "Vanilla World",
            "Conversion of basic world directories/files from a vanilla Minecraft server.");

    /**
     * Returns {@link ServerType#VANILLA}.
     *
     * @return {@link ServerType#VANILLA}
     */
    @Override
    public ServerType compatibleServerType()
    {
        return ServerType.VANILLA;
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
