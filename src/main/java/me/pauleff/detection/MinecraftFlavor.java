package me.pauleff.detection;

import me.pauleff.Main;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public enum MinecraftFlavor
{
    VANILLA("Vanilla"),
    LIGHT_MODDED("Lightly Modded (Bukkit,Paper,...)"),
    MODDED("Modded (Forge,Fabric,...)");

    private final String description;

        MinecraftFlavor(String description)
    {
        this.description = description;
    }

        @Override
    public String toString()
    {
        return description;
    }

        public String[] getFiles(Path worldDirectory, boolean justWorld)
    {
        ArrayList<String> filesAndFolders = new ArrayList<>();
        /*
         * This works for all flavors because the default directory for player connected data is always in the world folder.
         * Bukkit based servers (Paper, Spigot, etc.) do create extra folder like world_nether and world_the_end.
         * But the relevant information to convert is always saved in the overworld world folder.
         */
        ArrayList<String> defaultDirectories = new ArrayList<>();
        if (!justWorld)
        {
            defaultDirectories.add("./");
        }
        defaultDirectories.add("./" + worldDirectory + "/playerdata");
        defaultDirectories.add("./" + worldDirectory + "/advancements");
        defaultDirectories.add("./" + worldDirectory + "/stats");
        
        for (String path : defaultDirectories)
        {
            filesAndFolders.addAll(Main.config.getFolderContent(path));
        }
        
        if (Main.config.isFileSet())
        {
            List<String> pathList = Main.config.getPaths();
            if (!pathList.isEmpty()) filesAndFolders.addAll(pathList);
        }
        
        switch (this)
        {
            // TODO: Find more flavor specific directories
            case VANILLA:
                // defaultDirectories.add("some/vanillaspecific/path");
                filesAndFolders.addAll(Main.config.getPathsRecursively("./" + worldDirectory));
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
