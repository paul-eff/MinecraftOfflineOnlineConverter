package me.pauleff.converter;

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

    @Override
    public String toString()
    {
        return description;
    }
}
