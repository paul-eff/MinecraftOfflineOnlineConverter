package me.pauleff;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Minecraft player with a name and UUID.
 * Provides access to player details and ensures immutability.
 *
 * @author Paul Ferlitz
 */
public class Player {
    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    private final String name;
    private final UUID uuid;

    /**
     * Constructs a new Player instance.
     *
     * @param name The player's name.
     * @param uuid The player's UUID (offline or online).
     */
    public Player(String name, UUID uuid) {
        if (name == null || name.isEmpty()) {
            logger.error("Player name cannot be null or empty.");
            throw new IllegalArgumentException("Player name cannot be null or empty.");
        }
        if (uuid == null) {
            logger.error("Player UUID cannot be null.");
            throw new IllegalArgumentException("Player UUID cannot be null.");
        }

        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Retrieves the player's name.
     *
     * @return The player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the player's UUID.
     *
     * @return The player's {@link UUID}.
     */
    public UUID getUuid() {
        return uuid;
    }
}
