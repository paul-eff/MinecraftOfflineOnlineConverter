package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

import java.util.List;

public non-sealed interface MultiServerPlugin extends MOOCPlugin
{
    List<ServerType> compatibleServerTypes();

    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        ServerType serverType = ctx.serverType();
        return ctx.isConversionOperation()
                && serverType != null
                && compatibleServerTypes().contains(serverType);
    }
}
