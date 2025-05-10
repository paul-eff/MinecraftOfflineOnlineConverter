package me.pauleff.minecraftflavors;

import me.pauleff.exceptions.PathNotValidException;

import java.io.File;
import java.nio.file.Path;

public class MinecraftFlavorDetection
{

    private final File baseDirectory;

    public MinecraftFlavorDetection(String baseDirectory) throws PathNotValidException {
        File temp = new File(baseDirectory);
        if (temp.isDirectory())
        {
            this.baseDirectory = temp;
        }else
        {
            throw new PathNotValidException(baseDirectory);
        }
    }

    public MinecraftFlavorDetection() throws PathNotValidException {
        File temp = new File("./");
        if (temp.isDirectory())
        {
            this.baseDirectory = temp;
        }else
        {
            throw new PathNotValidException("./");
        }
    }

    public MinecraftFlavor detectMinecraftFlavor()
    {
        if(isVanilla())
        {
            return MinecraftFlavor.VANILLA;
        }else if (isLightlyModded())
        {
            return MinecraftFlavor.LIGHT_MODDED;
        }else
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

    private boolean isBukkitFlavored()
    {
        if (isLightlyModded())
        {
            boolean hasSpigotYml = hasFile("spigot.yml");

            // TODO: more detection criteria here...

            return hasSpigotYml;
        }
        return false;
    }

    private boolean isModded()
    {
        boolean hasMods = hasFolder("mods");

        // TODO: more detection criteria here...

        return hasMods;
    }

    private boolean vanillaWorld()
    {
        boolean isVanillaWorld = true;

        File[] subdirectories = this.baseDirectory.listFiles(File::isDirectory);
        if (subdirectories != null) {
            for (File subdirectory : subdirectories) {
                if (subdirectory.getName().contains("_nether") || subdirectory.getName().contains("_the_end"))
                {
                    isVanillaWorld = false;
                    break;
                }
            }
        }
        return isVanillaWorld;
    }

    private boolean hasFolder(String pathToFolder)
    {
        Path directoryPath = baseDirectory.toPath().resolve(pathToFolder);
        File directory = new File(directoryPath.toString());

        return directory.exists() && directory.isDirectory();
    }

    private boolean hasFile(String pathToFile)
    {
        Path directoryPath = baseDirectory.toPath().resolve(pathToFile);
        File directory = new File(directoryPath.toString());

        return directory.exists() && directory.isFile();
    }
}
