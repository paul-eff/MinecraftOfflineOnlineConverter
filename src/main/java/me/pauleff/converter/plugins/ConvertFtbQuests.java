package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.files.FileNames;
import me.pauleff.common.handlers.files.FileRenamer;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.ConverterV3;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.UUIDType;
import me.pauleff.converter.api.MultiServerPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.pauleff.common.handlers.uuid.MinecraftUuids.*;
import static me.pauleff.common.handlers.uuid.OnlineProfileLookup.onlineUuidToName;

/**
 * Converts FTB Quests progress SNBT files between online and offline player UUIDs.
 * <p>
 * Runs for modded servers during an online/offline conversion. Operates on UUID-named
 * progress files under the world's {@code ftbquests} folder; quest book definition files
 * are left unchanged.
 *
 * @see ConverterV3
 */
public class ConvertFtbQuests implements MultiServerPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "convert-ftb-quests",
            "Convert FTB Quests",
            "Converts FTB Quests progress SNBT files (uuid, name, claimed rewards) between online and offline mode.",
            55);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns {@link ServerType#MODDED} as the only compatible server type.
     *
     * @return a single-element list containing {@link ServerType#MODDED}
     */
    @Override
    public List<ServerType> compatibleServerTypes()
    {
        return List.of(ServerType.MODDED);
    }

    /**
     * Returns the {@code ftbquests} folder under the world directory.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the FTB Quests folder path
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.worldFolder().resolve("ftbquests"));
    }

    /**
     * Converts UUID-named progress SNBT files under each resolved FTB Quests folder.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing {@code ftbquests} directories to process
     * @throws IOException if reading or writing progress files fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        int converted = 0;
        for (Path ftbQuestsFolder : resolvedExistingTargets)
        {
            if (!Files.isDirectory(ftbQuestsFolder))
            {
                continue;
            }
            for (Path progressFile : listUuidNamedSnbtFiles(ftbQuestsFolder))
            {
                if (convertProgressFile(ctx, progressFile))
                {
                    converted++;
                }
            }
        }
        logger().info("Converted {} FTB Quests progress file(s).", converted);
    }

    /**
     * Lists {@code .snbt} files directly under {@code folder} whose base name is a valid UUID.
     *
     * @param folder the FTB Quests folder to scan
     * @return the matching progress file paths; never {@code null}
     * @throws IOException if listing the directory fails
     */
    private List<Path> listUuidNamedSnbtFiles(Path folder) throws IOException
    {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.snbt"))
        {
            for (Path path : stream)
            {
                if (!Files.isRegularFile(path))
                {
                    continue;
                }
                String baseName = FileNames.stripExtension(path.getFileName().toString());
                if (isValid(baseName))
                {
                    files.add(path);
                }
            }
        }
        return files;
    }

    /**
     * Converts a single progress SNBT file when its UUID matches the conversion direction.
     * <p>
     * Parse, rename, and write failures are logged and treated as a skip rather than
     * aborting the plugin.
     *
     * @param ctx  the shared conversion context
     * @param path the progress file to convert
     * @return {@code true} if the file was converted; {@code false} if it was skipped
     */
    private boolean convertProgressFile(PluginContext ctx, Path path)
    {
        String baseName = FileNames.stripExtension(path.getFileName().toString());
        UUID sourceUuid = UUID.fromString(baseName);
        UUIDType sourceType = typeOf(sourceUuid);

        if (!((ctx.conversionTarget() == ConversionTarget.ONLINE && sourceType == UUIDType.OFFLINE)
                || (ctx.conversionTarget() == ConversionTarget.OFFLINE && sourceType == UUIDType.ONLINE)))
        {
            logger().debug("Skipping {} — UUID type does not match conversion direction.", path.getFileName());
            return false;
        }

        try
        {
            UUID targetUuid = resolveTargetUuid(ctx, sourceUuid);
            if (targetUuid == null)
            {
                logger().warn("No mapping available for UUID {} in {}. Skipping.", sourceUuid, path.normalize());
                return false;
            }

            String snbt = Files.readString(path);
            Tag<?> root = SNBTUtil.fromSNBT(snbt, true);
            if (!(root instanceof CompoundTag compound))
            {
                logger().warn("Skipping {} — root tag is not a compound.", path.getFileName());
                return false;
            }

            updateIdentityFields(compound, targetUuid);
            String updatedSnbt = replaceMappedUuids(SNBTUtil.toSNBT(compound), ctx);

            Path renamed = FileRenamer.renamePreservingExtension(path, targetUuid.toString());
            Files.writeString(renamed, updatedSnbt);
            logger().info("Converted FTB Quests progress {} -> {}", sourceUuid, targetUuid);
            return true;
        } catch (IOException | RuntimeException e)
        {
            logger().error("Failed to convert FTB Quests file {}: {}", path.normalize(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Updates the {@code uuid} and {@code name} string tags to match the target UUID.
     * <p>
     * When present, {@code uuid} is written without dashes. When present, {@code name} keeps
     * the username before {@code #} and appends the first eight characters of the hyphenated
     * target UUID.
     *
     * @param compound   the progress compound tag
     * @param targetUuid the remapped player UUID
     */
    private void updateIdentityFields(CompoundTag compound, UUID targetUuid)
    {
        if (compound.containsKey("uuid"))
        {
            compound.putString("uuid", dashless(targetUuid));
        }

        if (compound.containsKey("name"))
        {
            String existingName = compound.getString("name");
            String username = existingName.contains("#")
                    ? existingName.substring(0, existingName.indexOf('#'))
                    : existingName;
            String prefix = targetUuid.toString().substring(0, 8);
            compound.putString("name", username + "#" + prefix);
        }
    }

    /**
     * Replaces every mapped UUID string in {@code snbt} with its remapped counterpart.
     * <p>
     * Both hyphenated and dashless forms are replaced so nested references remain consistent
     * with the renamed progress file.
     *
     * @param snbt the SNBT text to rewrite
     * @param ctx  the shared conversion context holding UUID mappings
     * @return the SNBT text with mapped UUIDs replaced
     */
    private String replaceMappedUuids(String snbt, PluginContext ctx)
    {
        String updated = snbt;
        for (Map.Entry<UUID, UUID> entry : ctx.uuidMap().entrySet())
        {
            UUID from = entry.getKey();
            UUID to = entry.getValue();
            if (to == null)
            {
                continue;
            }
            String fromHyphen = from.toString();
            String toHyphen = to.toString();
            updated = updated.replace(fromHyphen, toHyphen);
            updated = updated.replace(dashless(from), dashless(to));
        }
        return updated;
    }

    /**
     * Resolves the remapped UUID for a source UUID, consulting and possibly extending the context map.
     * <p>
     * Existing mappings win. Online conversion without a mapping returns {@code null}. Offline
     * conversion may look up the online name and derive an offline UUID, storing the new mapping
     * on the context.
     *
     * @param ctx        the shared conversion context
     * @param sourceUuid the UUID found in the progress file name
     * @return the target UUID, or {@code null} if no mapping can be determined
     * @throws IOException if an online name lookup fails
     * @see ConverterV3
     */
    private UUID resolveTargetUuid(PluginContext ctx, UUID sourceUuid) throws IOException
    {
        UUID targetUuid = ctx.getTargetUuid(sourceUuid);
        if (targetUuid != null)
        {
            return targetUuid;
        }

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
        logger().debug("Added new UUID mapping for {}: {} -> {}", playerName, sourceUuid, offlineUuid);
        return offlineUuid;
    }
}
