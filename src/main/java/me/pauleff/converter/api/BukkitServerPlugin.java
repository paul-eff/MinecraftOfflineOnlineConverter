package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;


public interface BukkitServerPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ServerType.BUKKIT == ctx.serverType();
    }
}
