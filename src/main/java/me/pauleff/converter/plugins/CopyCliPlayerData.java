package me.pauleff.converter.plugins;

import me.pauleff.Main;
import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.NBTHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        ParsedArguments parsedArguments = ctx.parsedArguments();
        return parsedArguments != null && parsedArguments.shouldCopyPlayerData();
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
                ctx.serverFolder(),
                Objects.requireNonNull(ctx.serverType(), "Server type must be detected before copying player data."));
    }

    private void copyPlayerData(Path sourceWorldFolder, Path destWorldFolder, Path serverFolder, ServerType serverType) throws IOException
    {
        Path relativeSourceWorldFolder = serverFolder.relativize(sourceWorldFolder);
        Path relativeDestWorldFolder = serverFolder.relativize(destWorldFolder);

        if (Files.isSameFile(sourceWorldFolder, destWorldFolder))
        {
            logger().warn("Could not move player data from {} to {}. Source and destination are the same folder",
                    relativeSourceWorldFolder, relativeDestWorldFolder);
            return;
        }

        if (Main.config.playerdataWorldBlacklist.contains(relativeSourceWorldFolder.toString()) ||
                Main.config.playerdataWorldBlacklist.contains(relativeDestWorldFolder.toString()))
        {
            logger().warn("Could not move player data from {} to {}. Source or destination folder is blacklisted.",
                    relativeSourceWorldFolder, relativeDestWorldFolder);
            return;
        }

        logger().info("Copying player data from {} to {}", relativeSourceWorldFolder, relativeDestWorldFolder);

        String[] allFiles = serverType.getFiles(relativeSourceWorldFolder, true);
        int movedFiles = 0;
        for (String relativePath : allFiles)
        {
            Path currentPath = serverFolder.resolve(relativePath).normalize();
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/"))
            {
                continue;
            }

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith))
            {
                continue;
            }
            logger().info("Processing file: {}", currentPath);
            try
            {
                Path tail = sourceWorldFolder.relativize(currentPath);
                Path finalPath = destWorldFolder.resolve(tail);

                if (currentPath.getParent().getFileName().toString().equals("playerdata"))
                {
                    if (NBTHandler.isNBTFile(currentFile))
                    {
                        logger().info("Copying NBT file to {}", destWorldFolder.normalize());
                        NBTHandler.copyPlayerDataNBT(currentPath, finalPath);
                        movedFiles++;
                        continue;
                    }
                } else
                {
                    String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());
                    if (!UUIDHandler.isValidUUID(fileName)) continue;
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
}
