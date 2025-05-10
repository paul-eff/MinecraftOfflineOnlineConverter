package me.pauleff.minecraftflavors;

import me.pauleff.exceptions.PathNotValidException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is responsible for detecting the flavor of a Minecraft server based on its directory structure and files.
 * It checks for various indicators to determine if the server is vanilla, lightly modded (e.g., Bukkit), or heavily modded (e.g., Forge).
 *
 * @author Paul Ferlitz
 */
public class MinecraftFlavorDetection
{
    private final Path baseDirectory;

    /**
     * Constructs a MinecraftFlavorDetection object with a specified base directory.
     *
     * @param baseDirectory The path to the base directory
     * @throws PathNotValidException if the provided path is not a valid directory
     */
    public MinecraftFlavorDetection(Path baseDirectory) throws PathNotValidException
    {
        if (Files.isDirectory(baseDirectory))
        {
            this.baseDirectory = baseDirectory;
        } else
        {
            throw new PathNotValidException(baseDirectory.toString());
        }
    }

    /**
     * Constructs a MinecraftFlavorDetection object with the current directory as the base directory.
     *
     * @throws PathNotValidException if the current directory is not valid
     */
    public MinecraftFlavorDetection() throws PathNotValidException
    {
        Path path = Path.of("./");
        if (Files.isDirectory(path))
        {
            this.baseDirectory = path;
        } else
        {
            throw new PathNotValidException(path.toString());
        }
    }

    /**
     * Detects the Minecraft flavor based on the directory structure and files.
     *
     * @return the detected Minecraft flavor
     */
    public MinecraftFlavor detectMinecraftFlavor()
    {
        if (isVanilla())
        {
            return MinecraftFlavor.VANILLA;
        } else if (isLightlyModded())
        {
            return MinecraftFlavor.LIGHT_MODDED;
        } else
        {
            return MinecraftFlavor.MODDED;
        }
    }

    /**
     * Checks if the Minecraft server is a vanilla server.
     *
     * @return true if the server is vanilla, false otherwise
     */
    private boolean isVanilla()
    {
        boolean isModded = isModded();
        boolean isBukkit = isLightlyModded();
        boolean vanillaStyleWorldFolder = vanillaWorld();

        return (!(isModded || isBukkit) && vanillaStyleWorldFolder);
    }

    /**
     * Checks if the Minecraft server is lightly modded (e.g., Bukkit).
     *
     * @return true if the server is lightly modded, false otherwise
     */
    private boolean isLightlyModded()
    {
        boolean isModded = isModded();
        boolean hasPlugins = hasFolder("plugins");
        boolean hasBukkitYml = hasFile("bukkit.yml");

        return (!isModded && hasPlugins && hasBukkitYml);
    }

    /**
     * Checks if the Minecraft server is modded.
     *
     * @return true if the server is modded, false otherwise
     */
    private boolean isModded()
    {
        // TODO: More detection criteria here...
        return hasFolder("mods");
    }

    /**
     * Checks if the world folder follows a vanilla style.
     *
     * @return true if the world folder is vanilla style, false otherwise
     */
    private boolean vanillaWorld()
    {
        // Get all folders in the server's base directory
        File[] subdirectories = baseDirectory.toFile().listFiles(File::isDirectory);

        if (subdirectories == null) return true;
        for (File dir : subdirectories)
        {
            String name = dir.getName();
            if (name.contains("_nether") || name.contains("_the_end"))
            {
                // If any folder contains "_nether" or "_the_end", it's not a vanilla world
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a specific folder exists within the base directory.
     *
     * @param folderName the relative path to the folder
     * @return true if the folder exists, false otherwise
     */
    private boolean hasFolder(String folderName)
    {
        Path directoryPath = baseDirectory.resolve(folderName);
        return Files.isDirectory(directoryPath);
    }

    /**
     * Checks if a specific file exists within the base directory.
     *
     * @param fileName the relative path to the file
     * @return true if the file exists, false otherwise
     */
    private boolean hasFile(String fileName)
    {
        Path filePath = baseDirectory.resolve(fileName);
        return Files.isRegularFile(filePath);
    }
}
