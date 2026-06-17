package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

import java.util.List;

/**
 * A {@link MOOCPlugin} registered for a specific {@link ServerType}. Plugins sharing a server type
 * run together in the conversion phase after discovery plugins have populated {@link PluginContext}.
 */
public interface MultiServerPlugin extends MOOCPlugin
{
    List<ServerType> getCompatibleServerTypes();

    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return getCompatibleServerTypes().contains(ctx.serverType());
    }
}
