package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.files.FileNames;
import me.pauleff.common.handlers.files.FileRenamer;
import me.pauleff.converter.ConversionTarget;
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

import static me.pauleff.common.handlers.UUIDHandler.*;

public class ConvertFtbQuests implements MultiServerPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "convert-ftb-quests",
            "Convert FTB Quests",
            "Converts FTB Quests progress SNBT files (uuid, name, claimed rewards) between online and offline mode.",
            55);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<ServerType> compatibleServerTypes()
    {
        return List.of(ServerType.MODDED);
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.worldFolder().resolve("ftbquests"));
    }

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
                if (isValidUUID(baseName))
                {
                    files.add(path);
                }
            }
        }
        return files;
    }

    private boolean convertProgressFile(PluginContext ctx, Path path)
    {
        String baseName = FileNames.stripExtension(path.getFileName().toString());
        UUID sourceUuid = UUID.fromString(baseName);
        UUIDType sourceType = getUUIDType(sourceUuid);

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

    private void updateIdentityFields(CompoundTag compound, UUID targetUuid)
    {
        String dashless = targetUuid.toString().replace("-", "");

        if (compound.containsKey("uuid"))
        {
            compound.putString("uuid", dashless);
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
            updated = updated.replace(fromHyphen.replace("-", ""), toHyphen.replace("-", ""));
        }
        return updated;
    }

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

        String playerName = onlineUUIDToName(sourceUuid);
        if (playerName == null || playerName.isBlank())
        {
            return null;
        }

        UUID offlineUuid = nameToOfflineUUID(playerName);
        ctx.putUuidMapping(sourceUuid, offlineUuid);
        logger().debug("Added new UUID mapping for {}: {} -> {}", playerName, sourceUuid, offlineUuid);
        return offlineUuid;
    }
}
