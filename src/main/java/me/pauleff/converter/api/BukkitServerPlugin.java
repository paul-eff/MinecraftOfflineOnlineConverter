package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;


public non-sealed interface BukkitServerPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation() && ServerType.BUKKIT == ctx.serverType();
    }
}
