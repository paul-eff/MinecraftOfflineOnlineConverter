package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;


public non-sealed interface ModdedServerPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ServerType.MODDED == ctx.serverType();
    }
}
