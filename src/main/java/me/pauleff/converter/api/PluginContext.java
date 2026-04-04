package me.pauleff.converter.api;

import me.pauleff.detection.MinecraftFlavor;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data passed into {@link MOOCPlugin}s. The same instance is passed through the pipeline, so all
 * components are shared by reference—especially {@link #uuidMap()}.
 *
 * @param serverFolder Absolute server root.
 * @param worldFolder  Absolute world folder ({@code level-name}).
 * @param toOnlineMode Converting to online when {@code true}, offline when {@code false}.
 * @param flavor       Detected server type.
 * @param uuidMap      Map of UUIDs and their converted counterparts.
 */
public record PluginContext(
        Path serverFolder,
        Path worldFolder,
        boolean toOnlineMode,
        MinecraftFlavor flavor,
        Map<UUID, UUID> uuidMap)
{
    public PluginContext
    {
        Objects.requireNonNull(serverFolder, "ServerFolder can't be null.");
        Objects.requireNonNull(worldFolder, "WorldFolder can't be null.");
        Objects.requireNonNull(flavor, "Flavour can't be null.");
        Objects.requireNonNull(uuidMap, "UuidReplacements can't be null.");
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
