package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;


public interface VanillaServerPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ServerType.VANILLA == ctx.serverType();
    }
}
