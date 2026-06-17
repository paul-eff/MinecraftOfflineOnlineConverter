package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

import java.util.List;

public interface MultiServerPlugin extends MOOCPlugin
{
    List<ServerType> getCompatibleServerTypes();

    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return getCompatibleServerTypes().contains(ctx.serverType());
    }
}
