package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.NBTHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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

    private static boolean isPlayerDataFile(Path path)
    {
        Path parent = path.getParent();
        Path grandParent = path.getParent().getParent();
        String parentName = parent.getFileName().toString();
        String grandParentName = grandParent.getFileName().toString();
        return "playerdata".equals(parentName) || ("data".equals(parentName) && "players".equals(grandParentName));
    }

    private static boolean hasIgnoredExtension(Path path)
    {
        String fileName = path.getFileName().toString();
        return IGNORED_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

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

        logger().warn("""
                WARNING! Read before proceeding:
                * You must ensure the source and destination servers are the same version (or at least compatible)
                * Copying player data between worlds can cause players to spawn inside solid blocks, etc. (player coordinates are copied without checking validity in new world)
                * Currently this feature only applies to player data, not pets or other events linked to players
                """);
        if (!confirmContinue())
        {
            logger().info("Player data copy cancelled.");
            return;
        }

        copyPlayerData(ctx, resolvedExistingTargets.get(0), resolvedExistingTargets.get(1));
    }

    private boolean confirmContinue() throws IOException
    {
        System.out.print("Continue copying player data? [y/N]: ");
        System.out.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
        String answer = reader.readLine();
        return answer != null && (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
    }

    private void copyPlayerData(PluginContext ctx, Path sourceWorldFolder, Path destWorldFolder) throws IOException
    {
        if (Files.isSameFile(sourceWorldFolder, destWorldFolder))
        {
            logger().warn("Could not move player data from {} to {}. Source and destination are the same folder",
                    sourceWorldFolder.normalize(), destWorldFolder.normalize());
            return;
        }

        Objects.requireNonNull(ctx.worldFolderStructure(), "World folder structure must be detected before copying player data.");

        List<Path> sourceRoots = ctx.worldFolderStructure().dimensionRootFolders(ctx.serverFolder(), sourceWorldFolder);
        List<Path> destRoots = ctx.worldFolderStructure().dimensionRootFolders(ctx.serverFolder(), destWorldFolder);
        if (sourceRoots.size() != destRoots.size())
        {
            logger().warn("Could not copy player data. Source and destination dimension layouts do not match.");
            return;
        }

        logger().info("Copying player data from {} to {}", sourceWorldFolder.normalize(), destWorldFolder.normalize());

        int movedFiles = 0;
        for (int i = 0; i < sourceRoots.size(); i++)
        {
            Path sourceRoot = sourceRoots.get(i);
            Path destRoot = destRoots.get(i);
            if (!Files.isDirectory(sourceRoot))
            {
                logger().debug("Skipping missing source dimension folder: {}", sourceRoot.normalize());
                continue;
            }
            if (!Files.isDirectory(destRoot))
            {
                logger().debug("Skipping missing destination dimension folder: {}", destRoot.normalize());
                continue;
            }

            for (Path currentPath : returnAllFilesInFolders(List.of(sourceRoot)))
            {
                if (!Files.isRegularFile(currentPath) || hasIgnoredExtension(currentPath))
                {
                    continue;
                }
                logger().info("Processing file: {}", currentPath);
                try
                {
                    Path finalPath = destRoot.resolve(sourceRoot.relativize(currentPath));

                    if (isPlayerDataFile(currentPath))
                    {
                        if (NBTHandler.isNBTFile(currentPath.toFile()))
                        {
                            logger().info("Copying NBT file to {}", finalPath.normalize());
                            Files.createDirectories(finalPath.getParent());
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

                    logger().info("Copying file to {}", finalPath.normalize());
                    Files.createDirectories(finalPath.getParent());
                    Files.copy(currentPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                    movedFiles++;
                } catch (IllegalArgumentException | IOException e)
                {
                    logger().debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
                }
            }
        }

        logger().info("Copied {} files to {}", movedFiles, destWorldFolder.normalize());
    }
}
