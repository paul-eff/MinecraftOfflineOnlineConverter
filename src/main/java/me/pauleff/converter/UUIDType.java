package me.pauleff.converter;

/**
 * Classifies a Minecraft player UUID as online, offline, or invalid by UUID version.
 *
 * @see me.pauleff.common.handlers.UUIDHandler#getUUIDType(java.util.UUID)
 */
public enum UUIDType
{
    /**
     * An online-mode (Mojang) UUID, typically version {@code 4}.
     */
    ONLINE,

    /**
     * An offline-mode UUID, typically version {@code 3}.
     */
    OFFLINE,

    /**
     * A UUID that is neither a recognized online nor offline Minecraft UUID.
     */
    INVALID
}
