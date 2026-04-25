package me.pauleff.converter.api;

import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.WorldFormat;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public final class PluginContext
{
    private final Path serverFolder;
    private final Path worldFolder;
    private final ConversionTarget conversionTarget;
    private ServerType serverType;
    private WorldFormat worldFormat;
    private final Map<UUID, UUID> uuidMap;

    public PluginContext(
            Path serverFolder,
            Path worldFolder,
            ConversionTarget conversionTarget,
            Map<UUID, UUID> uuidMap)
    {
        this.serverFolder = Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        this.worldFolder = Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        this.conversionTarget = Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        this.uuidMap = Objects.requireNonNull(uuidMap, "UUID map can't be null.");
    }

    public PluginContext(
            Path serverFolder,
            Path worldFolder,
            ConversionTarget conversionTarget,
            ServerType serverType,
            WorldFormat worldFormat,
            Map<UUID, UUID> uuidMap)
    {
        this.serverFolder = Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        this.worldFolder = Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        this.conversionTarget = Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        this.serverType = Objects.requireNonNull(serverType, "Server type can't be null.");
        this.worldFormat = Objects.requireNonNull(worldFormat, "World worldFormat can't be null.");
        this.uuidMap = Objects.requireNonNull(uuidMap, "UUID map can't be null.");
    }

    /**
     * Adds or overwrites a mapping in {@linkplain PluginContext#uuidMap}.
     */
    public void putUuidMapping(UUID from, UUID to)
    {
        uuidMap.put(
                Objects.requireNonNull(from, "Original UUID to put into map can't be null."),
                Objects.requireNonNull(to, "New UUID to put into map can't be null."));
    }

    /**
     * Target UUID for {@code from}, or {@code null} if absent.
     */
    public UUID getTargetUuid(UUID from)
    {
        return uuidMap.get(Objects.requireNonNull(from, "Original UUID to put into map can't be null."));
    }

    public Path serverFolder()
    {
        return serverFolder;
    }

    public Path worldFolder()
    {
        return worldFolder;
    }

    public ConversionTarget conversionTarget()
    {
        return conversionTarget;
    }

    public ServerType serverType()
    {
        return serverType;
    }

    public WorldFormat worldFormat() { return worldFormat; }

    public Map<UUID, UUID> uuidMap()
    {
        return uuidMap;
    }

    public void setServerType(ServerType serverType)
    {
        this.serverType = serverType;
    }

    public void setWorldFormat(WorldFormat worldFormat)
    {
        this.worldFormat = worldFormat;
    }
}
