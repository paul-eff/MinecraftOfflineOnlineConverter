package me.pauleff.minecraftflavors;

import me.pauleff.handlers.CustomPathParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing different Minecraft flavors.
 * Flavors are different types of Minecraft installations, such as Vanilla, Paper, Forge, ...
 * Each flavor has a description and a method to get relevant files.
 *
 * @author Paul Ferlitz
 */
public enum MinecraftFlavor
{
    VANILLA("Vanilla"),
    LIGHT_MODDED("Lightly Modded (Bukkit,Paper,...)"),
    MODDED("Modded (Forge,Fabric,...)");

    private final String description;

    /**
     * Constructor for MinecraftFlavor enum.
     *
     * @param description The description of the Minecraft flavor.
     */
    MinecraftFlavor(String description)
    {
        this.description = description;
    }

    /**
     * Returns the description of the Minecraft flavor.
     *
     * @return The description of the Minecraft flavor.
     */
    @Override
    public String toString()
    {
        return description;
    }

    /**
     * Returns an array of file paths relevant to the specified Minecraft flavor.
     *
     * @param baseDirectory The base directory where the Minecraft server is installed.
     * @param worldName     The name of the Minecraft world.
     * @return An array of file paths relevant to the specified Minecraft flavor.
     */
    public String[] getFiles(String baseDirectory, String worldName)
    {
        // TODO: Return an array of paths or files, not strings
        CustomPathParser cpp = new CustomPathParser(baseDirectory);
        ArrayList<String> filesAndFolders = new ArrayList<>();
        /*
         * This works for all flavors because the default directory for player connected data is always in the world folder.
         * Bukkit based servers (Paper, Spigot, etc.) do create extra folder like world_nether and world_the_end.
         * But the relevant information to convert is always saved in the overworld world folder.
         */
        ArrayList<String> defaultDirectories = new ArrayList<>();
        defaultDirectories.add("./");
        defaultDirectories.add("./" + worldName + "/playerdata");
        defaultDirectories.add("./" + worldName + "/advancements");
        defaultDirectories.add("./" + worldName + "/stats");
        // Get the content of the default directories
        for (String path : defaultDirectories)
        {
            filesAndFolders.addAll(cpp.getFolderContent(path));
        }
        // If a custom_paths.yml file exists, add the paths from it
        if (cpp.isFileSet())
        {
            List<String> pathList = cpp.getPaths();
            if (!pathList.isEmpty()) filesAndFolders.addAll(pathList);
        }
        // At last, add all flavor-specific paths
        switch (this)
        {
            // TODO: Find more flavor specific directories
            case VANILLA:
                // defaultDirectories.add("some/vanillaspecific/path");
                filesAndFolders.addAll(cpp.getPathsRecursively("./" + worldName));
                break;
            case LIGHT_MODDED:
                // defaultDirectories.add("some/lightmoddedspecific/path");
                break;
            case MODDED:
                // defaultDirectories.add("some/moddedspecific/path");
                break;
        }
        return filesAndFolders.toArray(String[]::new);
    }
}
