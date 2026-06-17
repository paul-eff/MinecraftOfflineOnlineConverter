package me.pauleff.converter.api;

public interface DefaultPlugin extends MOOCPlugin
{
    @Override
    default boolean isEnabled(PluginContext ctx)
    {
        return true;
    }
}