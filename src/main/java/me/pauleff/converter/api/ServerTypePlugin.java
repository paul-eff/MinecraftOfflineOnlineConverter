package me.pauleff.converter.api;

import me.pauleff.converter.ServerType;

/**
 * A {@link MOOCPlugin} that applies only to a single {@link ServerType}.
 * <p>
 * Enablement requires an active conversion operation and an exact match between
 * the context's server type and {@link #compatibleServerType()}.
 */
public non-sealed interface ServerTypePlugin extends MOOCPlugin
{
    /**
     * Returns the single server type this plugin supports.
     *
     * @return the compatible {@link ServerType}
     */
    ServerType compatibleServerType();

    /**
     * Returns {@code true} when a conversion is requested and the context's server type
     * equals {@link #compatibleServerType()}.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this plugin should run; {@code false} otherwise
     */
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation()
                && ctx.serverType() == compatibleServerType();
    }
}
