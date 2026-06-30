package me.pauleff.converter.api;

public non-sealed interface DefaultPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return true;
    }
}