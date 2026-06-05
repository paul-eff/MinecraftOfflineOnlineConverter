package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;

import java.util.List;

import static me.pauleff.converter.PluginRegistry.*;

/**
 * Enum representing different Minecraft server types.
 * Needed, as the different types heavily impact how and where files are saved.
 */
public enum ServerType
{
    VANILLA("Default vanilla Minecraft server"),
    BUKKIT("Server-side extensions via plugins (Bukkit,Paper,...)"),
    MODDED("Server- & client-side extensions via mods (Forge,Fabric,...)");

    private final String description;

    ServerType(String description)
    {
        this.description = description;
    }

    public List<MOOCPlugin> toPluginList() {
        return switch (this)
        {
            case VANILLA -> vanillaPlugins();
            case BUKKIT -> bukkitPlugins();
            case MODDED -> moddedPlugins();
        };
    }

    @Override
    public String toString()
    {
        return description;
    }
}
