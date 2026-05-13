package me.pauleff.converter;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.Files.isRegularFile;
import static java.util.Objects.requireNonNull;
import static me.pauleff.common.handlers.FileHandler.isTextBasedFile;
import static me.pauleff.common.handlers.FileHandler.stripFileExtension;
import static me.pauleff.common.handlers.UUIDHandler.*;

public class ConverterV3
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterV3.class);
    private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of(
            "mcr", "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
            "md", "snbt", "nbt", "zip", "cache", "png", "jpeg", "js", "DS_Store"
    );

    private final PluginContext ctx;

    public ConverterV3(PluginContext ctx)
    {
        this.ctx = requireNonNull(ctx, "PluginContext cannot be null");
    }

    public void convert(List<Path> toConvert)
    {
        LOGGER.info("Starting world conversion ({} --> {}) on {} files...",
                ctx.conversionTarget() == ConversionTarget.ONLINE ? ConversionTarget.OFFLINE.name() : ConversionTarget.ONLINE.name(),
                ctx.conversionTarget().name(),
                toConvert.size(),
                ctx.serverFolder());
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
                String fileName = stripFileExtension(currentPath.getFileName().toString());
                if (isValidUUID(fileName))
                {
                    discoveredValidFiles++;
                    UUID sourceUuid = UUID.fromString(fileName);
                    UUIDType sourceUuidType = getUUIDType(sourceUuid);
                    if (validConversionDirection(sourceUuidType))
                    {
                        UUID targetUuid = resolveTargetUuid(sourceUuid);
                        if (targetUuid == null)
                        {
                            LOGGER.warn("No mapping available for UUID {} in file {}. Skipping rename.",
                                    sourceUuid, currentPath.normalize());
                        } else
                        {
                            String extension = getFileExtension(currentPath);
                            FileHandler.renameFile(currentPath, targetUuid.toString());
                            currentPath = currentPath.getParent().resolve(targetUuid + extension);
                            renamedFiles++;
                            LOGGER.debug("Renamed file UUID {} -> {}", sourceUuid, targetUuid);
                        }
                    }
                }

                if (isRegularFile(currentPath) && isTextBasedFile(currentPath))
                {
                    // TODO 13.05.2026: Unnecessary double call of isValidUUID. Find nicer implementation.
                    if (!isValidUUID(fileName)) {
                        discoveredValidFiles++;
                    }
                    if (replaceUuidReferencesInTextFile(currentPath))
                    {
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

    private boolean validConversionDirection(UUIDType sourceUuidType)
    {
        return (ctx.conversionTarget() == ConversionTarget.ONLINE && sourceUuidType == UUIDType.OFFLINE)
                || (ctx.conversionTarget() == ConversionTarget.OFFLINE && sourceUuidType == UUIDType.ONLINE);
    }

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

        String playerName = onlineUUIDToName(sourceUuid);
        if (playerName == null || playerName.isBlank())
        {
            return null;
        }

        UUID offlineUuid = nameToOfflineUUID(playerName);
        ctx.putUuidMapping(sourceUuid, offlineUuid);
        LOGGER.debug("Added new UUID mapping for {}: {} -> {}", playerName, sourceUuid, offlineUuid);
        return offlineUuid;
    }

    private boolean replaceUuidReferencesInTextFile(Path textFile) throws IOException
    {
        String content = Files.readString(textFile);
        String updated = content;

        for (Map.Entry<UUID, UUID> entry : ctx.uuidMap().entrySet())
        {
            updated = updated.replace(entry.getKey().toString(), entry.getValue().toString());
        }

        if (updated.equals(content))
        {
            return false;
        }

        Files.writeString(textFile, updated);
        LOGGER.debug("Updated UUID references in text file: {}", textFile.normalize());
        return true;
    }

    private boolean hasIgnoredExtension(Path path)
    {
        String name = path.getFileName().toString();
        return IGNORED_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private String getFileExtension(Path path)
    {
        String filename = path.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
