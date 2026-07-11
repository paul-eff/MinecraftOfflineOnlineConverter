package me.pauleff.converter;

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
