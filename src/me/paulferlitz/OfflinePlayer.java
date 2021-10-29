package me.paulferlitz;

import java.util.UUID;

public class OfflinePlayer
{
    private final String name;
    private final UUID uuid;

    public OfflinePlayer(String name, UUID uuid)
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
