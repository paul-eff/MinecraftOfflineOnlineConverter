package me.pauleff.converter;

/**
 * Identifies the kind of Minecraft server installation being converted.
 * <p>
 * Each constant carries a human-readable description returned by {@link #toString()}.
 */
public enum ServerType
{
    /**
     * A default vanilla Minecraft server.
     */
    VANILLA("Default vanilla Minecraft server"),

    /**
     * A Bukkit-based server with server-side plugins (Bukkit, Paper, and similar).
     */
    BUKKIT("Server-side extensions via plugins (Bukkit,Paper,...)"),

    /**
     * A modded server with server- and client-side mods (Forge, Fabric, and similar).
     */
    MODDED("Server- & client-side extensions via mods (Forge,Fabric,...)");

    private final String description;

    /**
     * Creates a server type with the given human-readable description.
     *
     * @param description the description shown by {@link #toString()}
     */
    ServerType(String description)
    {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this server type.
     *
     * @return the description associated with this constant
     */
    @Override
    public String toString()
    {
        return description;
    }
}
