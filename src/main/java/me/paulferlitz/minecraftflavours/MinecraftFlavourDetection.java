package me.paulferlitz.minecraftflavours;

import me.paulferlitz.exceptions.PathNotValidException;

import java.io.File;
import java.nio.file.Path;

public class MinecraftFlavourDetection {

    private final File baseDirectory;

    public MinecraftFlavourDetection(String baseDirectory) throws PathNotValidException {
        File temp = new File(baseDirectory);
        if (temp.isDirectory())
        {
            this.baseDirectory = temp;
        }else
        {
            throw new PathNotValidException(baseDirectory);
        }
    }

    public MinecraftFlavourDetection() throws PathNotValidException {
        File temp = new File("./");
        if (temp.isDirectory())
        {
            this.baseDirectory = temp;
        }else
        {
            throw new PathNotValidException("./");
        }
    }

    public MinecraftFlavour detectMinecraftFlavour()
    {
        if(isVanilla())
        {
            return MinecraftFlavour.VANILLA;
        }else if (isLightlyModded())
        {
            return MinecraftFlavour.LIGHT_MODDED;
        }else
        {
            return MinecraftFlavour.MODDED;
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

    private boolean isBukkitFlavoured()
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
