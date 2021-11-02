package me.paulferlitz;

import java.util.UUID;

public class Player
{
    private final String name;
    private final UUID uuid;

    public Player(String name, UUID uuid)
    {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
