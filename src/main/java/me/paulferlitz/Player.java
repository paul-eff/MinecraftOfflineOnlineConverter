package me.paulferlitz;

import java.util.UUID;

/**
 * Class to represent a player.
 */
public class Player
{
    // Class variables
    private final String name;
    private final UUID uuid;

    /**
     * Main constructor.
     *
     * @param name Player's name.
     * @param uuid Players's UUID (offline or online).
     */
    public Player(String name, UUID uuid)
    {
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Method to get player's name.
     *
     * @return The player name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method to get player's UUID.
     *
     * @return The player UUUID.
     */
    public UUID getUuid()
    {
        return uuid;
    }
}
