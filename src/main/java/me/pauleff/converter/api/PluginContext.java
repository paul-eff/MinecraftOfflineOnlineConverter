package me.pauleff.converter.api;

import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.SaveFileFormat;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.WorldFolderStructure;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public final class PluginContext
{
    private final Path serverFolder;
    private final Path worldFolder;
    private final ConversionTarget conversionTarget;
    private ServerType serverType;
    private WorldFolderStructure worldFolderStructure;
    private SaveFileFormat saveFileFormat;
    private final Map<UUID, UUID> uuidMap;

    public PluginContext(
            Path serverFolder,
            Path worldFolder,
            ConversionTarget conversionTarget)
    {
        this.serverFolder = Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        this.worldFolder = Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        this.conversionTarget = Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        this.uuidMap = new HashMap<>();
    }

    public PluginContext(
            Path serverFolder,
            Path worldFolder,
            ConversionTarget conversionTarget,
            ServerType serverType,
            WorldFolderStructure worldFolderStructure,
            SaveFileFormat saveFileFormat,
            Map<UUID, UUID> uuidMap)
    {
        this.serverFolder = Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        this.worldFolder = Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        this.conversionTarget = Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        this.serverType = serverType;
        this.worldFolderStructure = worldFolderStructure;
        this.saveFileFormat = saveFileFormat;
        this.uuidMap = uuidMap;
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

    public WorldFolderStructure worldFolderStructure()
    {
        return worldFolderStructure;
    }

    public SaveFileFormat worldSaveFileFormat() { return saveFileFormat; }

    public Map<UUID, UUID> uuidMap()
    {
        return uuidMap;
    }

    public void setServerType(ServerType serverType)
    {
        this.serverType = serverType;
    }

    public void setWorldFolderStructure(WorldFolderStructure worldFolderStructure)
    {
        this.worldFolderStructure = worldFolderStructure;
    }

    public void setSaveFileFormat(SaveFileFormat saveFileFormat)
    {
        this.saveFileFormat = saveFileFormat;
    }

    @Override
    public String toString()
    {
        return "PluginContext{" +
                "serverFolder=" + serverFolder +
                ", worldFolder=" + worldFolder +
                ", conversionTarget=" + conversionTarget +
                ", serverType=" + serverType +
                ", worldFolderStructure=" + worldFolderStructure +
                ", saveFileFormat=" + saveFileFormat +
                ", uuidMap=" + uuidMap +
                '}';
    }
}
