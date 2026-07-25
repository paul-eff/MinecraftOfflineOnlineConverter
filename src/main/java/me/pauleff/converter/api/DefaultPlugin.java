package me.pauleff.converter.api;

/**
 * A {@link MOOCPlugin} that is always enabled regardless of conversion mode or server type.
 * <p>
 * Suitable for plugins that should run on every invocation, such as detection or
 * property-update steps that are not tied to a specific server implementation.
 */
public non-sealed interface DefaultPlugin extends MOOCPlugin
{
    /**
     * Always returns {@code true}, so the plugin runs for every {@link PluginContext}.
     *
     * @param ctx the shared conversion context (unused)
     * @return {@code true} always
     */
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return true;
    }
}
