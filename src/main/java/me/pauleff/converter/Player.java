package me.pauleff.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Represents a Minecraft player with a name and UUID.
 * Provides access to player details and ensures immutability.
 *
 * @param name    Player name
 * @param newUUID the UUID we want to have assigned to the player
 */
public record Player(String name, UUID newUUID)
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    /**
     * Constructs a new Player instance.
     *
     * @param name    The player's name.
     * @param newUUID The UUID we want to reassign to the player (offline or online).
     */
    public Player
    {
        // Log error and throw exception if name is null or empty
        if (name == null || name.isEmpty())
        {
            String msg = "Player name cannot be null or empty.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // Log error and throw exception if uuid is null
        if (newUUID == null)
        {
            String msg = "Player UUID cannot be null.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // Otherwise create Player
    }

    /**
     * Retrieves the player's name.
     *
     * @return The player's name.
     */
    @Override
    public String name()
    {
        return name;
    }

    /**
     * Retrieves the player's new UUID.
     *
     * @return The player's {@link UUID}.
     */
    @Override
    public UUID newUUID()
    {
        return newUUID;
    }
}
