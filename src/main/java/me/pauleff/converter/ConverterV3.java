package me.pauleff.converter;

import me.pauleff.common.handlers.files.FileNames;
import me.pauleff.common.handlers.files.FileRenamer;
import me.pauleff.common.handlers.files.TextFileDetector;
import me.pauleff.converter.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.nio.file.Files.isRegularFile;
import static java.util.Objects.requireNonNull;
import static me.pauleff.common.handlers.uuid.MinecraftUuids.*;
import static me.pauleff.common.handlers.uuid.OnlineProfileLookup.onlineUuidToName;

/**
 * Converts Minecraft world and player files between online and offline UUID modes.
 * <p>
 * Renames files whose base name is a convertible UUID and rewrites UUID string
 * references inside text-based files, using mappings from the shared {@link PluginContext}.
 *
 * @see PluginContext
 * @see ConversionTarget
 */
public final class ConverterV3
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterV3.class);
    private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of(
            "mcr", "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
            "md", "snbt", "nbt", "zip", "cache", "png", "jpeg", "js", "DS_Store"
    );

    private final PluginContext ctx;

    /**
     * Creates a converter bound to the given plugin context.
     *
     * @param ctx the shared conversion context supplying the target mode and UUID map
     * @throws NullPointerException if {@code ctx} is {@code null}
     */
    public ConverterV3(PluginContext ctx)
    {
        this.ctx = requireNonNull(ctx, "PluginContext cannot be null");
    }

    /**
     * Converts the given files according to the context's {@link ConversionTarget}.
     * <p>
     * Skips non-regular files and paths with ignored extensions. For each remaining file,
     * renames UUID-named files when a mapping exists and the UUID version matches the
     * conversion direction, then replaces mapped UUID strings in text-based content.
     * Per-file errors are logged and processing continues.
     *
     * @param toConvert the candidate file paths to process
     */
    public void convert(List<Path> toConvert)
    {
        LOGGER.info("Starting world conversion ({} --> {}) on {} files...",
                ctx.conversionTarget() == ConversionTarget.ONLINE ? ConversionTarget.OFFLINE.name() : ConversionTarget.ONLINE.name(),
                ctx.conversionTarget().name(),
                toConvert.size());
        int discoveredValidFiles = 0;
        int renamedFiles = 0;
        int updatedTextFiles = 0;

        for (Path originalPath : toConvert)
        {
            if (!isRegularFile(originalPath) || hasIgnoredExtension(originalPath))
            {
                continue;
            }

            LOGGER.debug("Processing file: {}", originalPath.normalize());
            Path currentPath = originalPath;
            try
            {
                String fileName = FileNames.stripExtension(currentPath.getFileName().toString());
                if (isValid(fileName))
                {
                    discoveredValidFiles++;
                    UUID sourceUuid = UUID.fromString(fileName);
                    UUIDType sourceUuidType = typeOf(sourceUuid);
                    if (validConversionDirection(sourceUuidType))
                    {
                        UUID targetUuid = resolveTargetUuid(sourceUuid);
                        if (targetUuid == null)
                        {
                            LOGGER.warn("No mapping available for UUID {} in file {}. Skipping rename.",
                                    sourceUuid, currentPath.normalize());
                        } else
                        {
                            currentPath = FileRenamer.renamePreservingExtension(currentPath, targetUuid.toString());
                            renamedFiles++;
                            LOGGER.debug("Renamed file UUID {} -> {}", sourceUuid, targetUuid);
                        }
                    }
                }

                if (isRegularFile(currentPath) && TextFileDetector.isTextBased(currentPath))
                {
                    if (replaceUuidReferencesInTextFile(currentPath))
                    {
                        discoveredValidFiles++;
                        updatedTextFiles++;
                    }
                }
            } catch (IllegalArgumentException | IOException e)
            {
                LOGGER.error("Skipping file {} due to an error: {}",
                        currentPath.normalize(), e.getMessage());
            }
        }

        LOGGER.info("Renamed {} UUID file(s) & updated {} file's content(s). Processed {} relevant file(s).", renamedFiles, updatedTextFiles, discoveredValidFiles);
    }

    /**
     * Indicates whether the source UUID version matches the context's conversion direction.
     *
     * @param sourceUuidType the classified type of the source UUID
     * @return {@code true} if the UUID should be converted for this run; {@code false} otherwise
     */
    private boolean validConversionDirection(UUIDType sourceUuidType)
    {
        return (ctx.conversionTarget() == ConversionTarget.ONLINE && sourceUuidType == UUIDType.OFFLINE)
                || (ctx.conversionTarget() == ConversionTarget.OFFLINE && sourceUuidType == UUIDType.ONLINE);
    }

    /**
     * Resolves the remapped UUID for a source UUID, consulting and possibly extending the context map.
     * <p>
     * Returns an existing mapping when present. For online conversion with no mapping, returns
     * {@code null} (offline UUIDs cannot be inferred without a name source such as usercache).
     * For offline conversion, looks up the online name and derives an offline UUID, storing the
     * new mapping on the context.
     *
     * @param sourceUuid the UUID found in a file name
     * @return the target UUID, or {@code null} if no mapping can be determined
     * @throws IOException if an online name lookup fails
     */
    private UUID resolveTargetUuid(UUID sourceUuid) throws IOException
    {
        UUID targetUuid = ctx.getTargetUuid(sourceUuid);
        if (targetUuid != null)
        {
            return targetUuid;
        }

        /*
         * Short explanation:
         * When converting from online to offline, there should be no interference.
         * But when converting from offline to online we encounter the problem, that there is no way to infer the online username/UUID from an offline UUID.
         * The PrefetchUsercache plugin iterates over usercache.json during MOOC setup, being the for now only file which maps offline name to offline UUID.
         * Therefor being the only true source for converting offline to online UUIDs.
         *
         * NOTE: If this is not true, feel free to contribute!
         */
        if (ctx.conversionTarget() == ConversionTarget.ONLINE)
        {
            return null;
        }

        String playerName = onlineUuidToName(sourceUuid);
        if (playerName == null || playerName.isBlank())
        {
            return null;
        }

        UUID offlineUuid = offlineFromName(playerName);
        ctx.putUuidMapping(sourceUuid, offlineUuid);
        LOGGER.debug("Added new UUID mapping for {}: {} -> {}", playerName, sourceUuid, offlineUuid);
        return offlineUuid;
    }

    /**
     * Replaces mapped UUID string occurrences in a text file's content.
     *
     * @param textFile the text-based file to update
     * @return {@code true} if the file content changed; {@code false} otherwise
     * @throws IOException if reading or writing the file fails
     */
    private boolean replaceUuidReferencesInTextFile(Path textFile) throws IOException
    {
        String content = Files.readString(textFile);
        String updated = content;

        for (Map.Entry<UUID, UUID> entry : ctx.uuidMap().entrySet())
        {
            UUID targetUuid = ctx.getTargetUuid(entry.getKey());
            if (targetUuid == null)
            {
                continue;
            }
            updated = updated.replace(entry.getKey().toString(), targetUuid.toString());
        }

        if (updated.equals(content))
        {
            return false;
        }

        Files.writeString(textFile, updated);
        LOGGER.debug("Updated UUID references in text file: {}", textFile.normalize());
        return true;
    }

    /**
     * Indicates whether the path's file name ends with an ignored extension.
     *
     * @param path the path to check
     * @return {@code true} if the file should be skipped; {@code false} otherwise
     */
    private boolean hasIgnoredExtension(Path path)
    {
        String name = path.getFileName().toString();
        return IGNORED_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
