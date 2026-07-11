package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

public non-sealed interface ServerTypePlugin extends MOOCPlugin
{
    ServerType compatibleServerType();

    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation()
                && ctx.serverType() == compatibleServerType();
    }
}
