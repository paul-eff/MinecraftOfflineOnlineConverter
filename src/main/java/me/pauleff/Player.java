package me.pauleff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Represents a Minecraft player with a name and UUID.
 * Provides access to player details and ensures immutability.
 *
 * @author Paul Ferlitz
 */
public class Player
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    private final String name;
    private final UUID uuid;

    /**
     * Constructs a new Player instance.
     *
     * @param name The player's name.
     * @param uuid The player's UUID (offline or online).
     */
    public Player(String name, UUID uuid)
    {
        // Log error and throw exception if name is null or empty
        if (name == null || name.isEmpty())
        {
            String msg = "Player name cannot be null or empty.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // Log error and throw exception if uuid is null
        if (uuid == null)
        {
            String msg = "Player UUID cannot be null.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // Otherwise create Player
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Retrieves the player's name.
     *
     * @return The player's name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the player's UUID.
     *
     * @return The player's {@link UUID}.
     */
    public UUID getUuid()
    {
        return uuid;
    }
}
