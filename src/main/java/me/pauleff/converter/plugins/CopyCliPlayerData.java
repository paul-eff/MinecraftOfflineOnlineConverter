package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.NBTHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.common.handlers.files.FileNames;
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

/**
 * Copies player data from a CLI-specified source world into the current world.
 * <p>
 * Enabled when {@code -copy <world-name>} is set. Prompts for confirmation before
 * copying playerdata and UUID-named files across matching dimension roots.
 */
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

    /**
     * Indicates whether the path is under a {@code playerdata} or {@code players/data} directory.
     *
     * @param path the file path to inspect
     * @return {@code true} if the path looks like player data; {@code false} otherwise
     */
    private static boolean isPlayerDataFile(Path path)
    {
        Path parent = path.getParent();
        Path grandParent = path.getParent().getParent();
        String parentName = parent.getFileName().toString();
        String grandParentName = grandParent.getFileName().toString();
        return "playerdata".equals(parentName) || ("data".equals(parentName) && "players".equals(grandParentName));
    }

    /**
     * Indicates whether the file name ends with an ignored extension.
     *
     * @param path the file path to inspect
     * @return {@code true} if the extension should be skipped; {@code false} otherwise
     */
    private static boolean hasIgnoredExtension(Path path)
    {
        String fileName = path.getFileName().toString();
        return IGNORED_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns {@code true} when the {@code -copy} CLI option was provided.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if player-data copying was requested; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.parsedArguments().shouldCopyPlayerData();
    }

    /**
     * Returns the resolved source world folder and the destination world folder.
     *
     * @param ctx the shared conversion context
     * @return a two-element list of source then destination world paths
     */
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

    /**
     * Prompts for confirmation and copies player data from the source world to the destination.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets expected to contain source and destination world folders
     * @throws IOException if confirmation I/O or file copying fails
     */
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

    /**
     * Asks the user on standard input whether to continue with the copy.
     *
     * @return {@code true} if the answer is {@code y} or {@code yes}; {@code false} otherwise
     * @throws IOException if reading from standard input fails
     */
    private boolean confirmContinue() throws IOException
    {
        System.out.print("Continue copying player data? [y/N]: ");
        System.out.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
        String answer = reader.readLine();
        return answer != null && (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
    }

    /**
     * Copies playerdata and UUID-named files between matching dimension roots of two worlds.
     *
     * @param ctx               the shared conversion context (must have a detected world folder structure)
     * @param sourceWorldFolder the source world root
     * @param destWorldFolder   the destination world root
     * @throws IOException if comparing or copying files fails
     */
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
                logger().debug("Processing file: {}", currentPath);
                try
                {
                    Path finalPath = destRoot.resolve(sourceRoot.relativize(currentPath));

                    if (isPlayerDataFile(currentPath))
                    {
                        if (NBTHandler.isNBTFile(currentPath.toFile()))
                        {
                            logger().debug("Copying NBT file to {}", finalPath.normalize());
                            Files.createDirectories(finalPath.getParent());
                            NBTHandler.copyPlayerDataNBT(currentPath, finalPath);
                            movedFiles++;
                            continue;
                        }
                    } else
                    {
                        String fileName = FileNames.stripExtension(currentPath.getFileName().toString());
                        if (!UUIDHandler.isValidUUID(fileName))
                        {
                            continue;
                        }
                    }

                    logger().debug("Copying file to {}", finalPath.normalize());
                    Files.createDirectories(finalPath.getParent());
                    Files.copy(currentPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                    movedFiles++;
                } catch (IllegalArgumentException | IOException e)
                {
                    logger().warn("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
                }
            }
        }

        logger().info("Copied {} files to {}", movedFiles, destWorldFolder.normalize());
    }
}
