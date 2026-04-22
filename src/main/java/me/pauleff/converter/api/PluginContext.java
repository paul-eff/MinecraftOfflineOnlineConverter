package me.pauleff.converter.api;

import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.WorldFormat;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data passed into {@link MOOCPlugin}s. The same instance is passed through the pipeline, so all
 * components are shared by reference—especially {@link #uuidMap()}.
 *
 * @param serverFolder     Absolute server root.
 * @param worldFolder      Absolute world folder ({@code level-name}).
 * @param conversionTarget What target to convert the server to.
 * @param serverType       Detected server type.
 * @param worldFormat      Detected server world worldFormat.
 * @param uuidMap          Map of UUIDs and their converted counterparts.
 */
public record PluginContext(
        Path serverFolder,
        Path worldFolder,
        ConversionTarget conversionTarget,
        ServerType serverType,
        WorldFormat worldFormat,
        Map<UUID, UUID> uuidMap)
{
    public PluginContext
    {
        Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        Objects.requireNonNull(serverType, "Server type can't be null.");
        Objects.requireNonNull(worldFormat, "World worldFormat can't be null.");
        Objects.requireNonNull(uuidMap, "UUID map can't be null.");
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
}
