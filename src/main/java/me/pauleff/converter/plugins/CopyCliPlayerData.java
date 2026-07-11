package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.NBTHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class CopyCliPlayerData implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "copy-cli-player-data",
            "Copy CLI Player Data",
            "Copies player data from a source world to the current world when -copy <world-name> is set.",
            5);

    private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of(
            "mcr", "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
            "md", "snbt", "nbt", "zip", "cache", "png", "jpeg", "js", "DS_Store"
    );

    private static final List<String> PLAYER_DATA_SUBFOLDERS = List.of("playerdata", "advancements", "stats");

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.parsedArguments().shouldCopyPlayerData();
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        String sourceWorld = ctx.parsedArguments().copyPlayerDataSourceWorld().orElseThrow();
        Path relativeSource = Paths.get(sourceWorld);
        if (relativeSource.isAbsolute())
        {
            relativeSource = relativeSource.getRoot().relativize(relativeSource);
        }
        return List.of(
                ctx.serverFolder().resolve(relativeSource).toAbsolutePath().normalize(),
                ctx.worldFolder());
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        if (resolvedExistingTargets.size() != 2)
        {
            logger().warn("Could not copy player data. Both source and destination world folders must exist.");
            return;
        }

        copyPlayerData(
                resolvedExistingTargets.get(0),
                resolvedExistingTargets.get(1),
                Objects.requireNonNull(ctx.serverType(), "Server type must be detected before copying player data."));
    }

    private void copyPlayerData(Path sourceWorldFolder, Path destWorldFolder, ServerType serverType) throws IOException
    {
        if (Files.isSameFile(sourceWorldFolder, destWorldFolder))
        {
            logger().warn("Could not move player data from {} to {}. Source and destination are the same folder",
                    sourceWorldFolder.normalize(), destWorldFolder.normalize());
            return;
        }

        logger().info("Copying player data from {} to {}", sourceWorldFolder.normalize(), destWorldFolder.normalize());

        int movedFiles = 0;
        for (Path currentPath : collectPlayerDataFiles(sourceWorldFolder, serverType))
        {
            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentPath.toString().replace('\\', '/').contains("/region/"))
            {
                continue;
            }

            if (!Files.isRegularFile(currentPath) || hasIgnoredExtension(currentPath))
            {
                continue;
            }
            logger().info("Processing file: {}", currentPath);
            try
            {
                Path finalPath = destWorldFolder.resolve(sourceWorldFolder.relativize(currentPath));

                if (isPlayerDataFile(currentPath))
                {
                    if (NBTHandler.isNBTFile(currentPath.toFile()))
                    {
                        logger().info("Copying NBT file to {}", destWorldFolder.normalize());
                        NBTHandler.copyPlayerDataNBT(currentPath, finalPath);
                        movedFiles++;
                        continue;
                    }
                } else
                {
                    String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());
                    if (!UUIDHandler.isValidUUID(fileName))
                    {
                        continue;
                    }
                }

                logger().info("Copying file to {}", destWorldFolder.normalize());
                Files.copy(currentPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                movedFiles++;
            } catch (IllegalArgumentException | IOException e)
            {
                logger().debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
            }
        }

        logger().info("Copied {} files to {}", movedFiles, destWorldFolder.normalize());
    }

    private List<Path> collectPlayerDataFiles(Path sourceWorldFolder, ServerType serverType) throws IOException
    {
        Set<Path> files = new LinkedHashSet<>();
        for (String subfolder : PLAYER_DATA_SUBFOLDERS)
        {
            Path dir = sourceWorldFolder.resolve(subfolder);
            if (!Files.isDirectory(dir))
            {
                continue;
            }
            try (Stream<Path> stream = Files.list(dir))
            {
                stream.filter(Files::isRegularFile).map(Path::normalize).forEach(files::add);
            }
        }
        if (serverType == ServerType.VANILLA)
        {
            try (Stream<Path> stream = Files.walk(sourceWorldFolder))
            {
                stream.filter(Files::isRegularFile).map(Path::normalize).forEach(files::add);
            }
        }
        return List.copyOf(files);
    }

    private static boolean isPlayerDataFile(Path path)
    {
        Path parent = path.getParent();
        return parent != null && "playerdata".equals(parent.getFileName().toString());
    }

    private static boolean hasIgnoredExtension(Path path)
    {
        String fileName = path.getFileName().toString();
        return IGNORED_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
