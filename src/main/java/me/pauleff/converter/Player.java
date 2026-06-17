package me.pauleff.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public record Player(String name, UUID newUUID)
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    public Player
    {
        if (name == null || name.isEmpty())
        {
            String msg = "Player name cannot be null or empty.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (newUUID == null)
        {
            String msg = "Player UUID cannot be null.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public UUID newUUID()
    {
        return newUUID;
    }
}
