package me.pauleff.detection;

import me.pauleff.common.exceptions.PathNotValidException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class MinecraftFlavorDetection
{
    private final Path baseDirectory;

    public MinecraftFlavorDetection(Path baseDirectory) throws PathNotValidException
    {
        if (Files.isDirectory(baseDirectory))
        {
            this.baseDirectory = baseDirectory;
        } else
        {
            throw new PathNotValidException(baseDirectory.normalize());
        }
    }

    public MinecraftFlavorDetection() throws PathNotValidException
    {
        Path path = Path.of("./");
        if (Files.isDirectory(path))
        {
            this.baseDirectory = path;
        } else
        {
            throw new PathNotValidException(path.normalize());
        }
    }

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

    private boolean isVanilla()
    {
        boolean isModded = isModded();
        boolean isBukkit = isLightlyModded();
        boolean vanillaStyleWorldFolder = vanillaWorld();

        return (!(isModded || isBukkit) && vanillaStyleWorldFolder);
    }

    private boolean isLightlyModded()
    {
        boolean isModded = isModded();
        boolean hasPlugins = hasFolder("plugins");
        boolean hasBukkitYml = hasFile("bukkit.yml");

        return (!isModded && hasPlugins && hasBukkitYml);
    }

    private boolean isModded()
    {
        // TODO: More detection criteria here...
        return hasFolder("mods");
    }

    private boolean vanillaWorld()
    {
        File[] subdirectories = baseDirectory.toFile().listFiles(File::isDirectory);

        if (subdirectories == null) return true;
        for (File dir : subdirectories)
        {
            String name = dir.getName();
            if (name.contains("_nether") || name.contains("_the_end"))
            {
                return false;
            }
        }
        return true;
    }

    private boolean hasFolder(String folderName)
    {
        Path directoryPath = baseDirectory.resolve(folderName);
        return Files.isDirectory(directoryPath);
    }

    private boolean hasFile(String fileName)
    {
        Path filePath = baseDirectory.resolve(fileName);
        return Files.isRegularFile(filePath);
    }
}
