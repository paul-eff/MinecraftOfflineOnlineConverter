package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

import java.util.List;

/**
 * A {@link MOOCPlugin} that applies to one or more {@link ServerType} values.
 * <p>
 * Enablement requires an active conversion operation and that the context's
 * server type is contained in {@link #compatibleServerTypes()}.
 */
public non-sealed interface MultiServerPlugin extends MOOCPlugin
{
    /**
     * Returns the server types this plugin supports.
     *
     * @return the list of compatible {@link ServerType} values; never {@code null}
     */
    List<ServerType> compatibleServerTypes();

    /**
     * Returns {@code true} when a conversion is requested, the context has a non-{@code null}
     * server type, and that type is listed by {@link #compatibleServerTypes()}.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this plugin should run; {@code false} otherwise
     */
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        ServerType serverType = ctx.serverType();
        return ctx.isConversionOperation()
                && serverType != null
                && compatibleServerTypes().contains(serverType);
    }
}
