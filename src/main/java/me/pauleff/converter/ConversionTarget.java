package me.pauleff.converter;

/**
 * Indicates whether a conversion run remaps player data toward online or offline mode.
 */
public enum ConversionTarget
{
    /**
     * Convert player UUIDs and references toward online (Mojang) mode.
     */
    ONLINE,

    /**
     * Convert player UUIDs and references toward offline mode.
     */
    OFFLINE
}
