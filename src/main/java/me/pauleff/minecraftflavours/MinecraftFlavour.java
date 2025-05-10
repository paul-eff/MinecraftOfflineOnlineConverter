package me.pauleff.minecraftflavours;

import me.pauleff.handlers.CustomPathParser;

import java.util.ArrayList;
import java.util.List;

public enum MinecraftFlavour
{
    VANILLA("Vanilla"),
    LIGHT_MODDED("Lightly Modded (Bukkit,Paper,...)"),
    MODDED("Modded (Forge,Fabric,...)");

    private final String description;

    MinecraftFlavour(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }

    public String[] getFiles(String baseDirectory, String worldName)
    {
        CustomPathParser cpp = new CustomPathParser(baseDirectory);
        ArrayList<String> filesAndFolders = new ArrayList<>();

        ArrayList<String> defaultDirectories = new ArrayList<>();
        defaultDirectories.add("./");
        defaultDirectories.add("./" + worldName + "/playerdata");
        defaultDirectories.add("./" + worldName + "/advancements");
        defaultDirectories.add("./" + worldName + "/stats");

        for (String path : defaultDirectories)
        {
            filesAndFolders.addAll(cpp.getFolderContent(path));
        }

        if (cpp.isFileSet())
        {
            List<String> pathList = cpp.getPaths();
            if (!pathList.isEmpty()) filesAndFolders.addAll(pathList);
        }

        switch (this)
        {
            // TODO: Find flavour specific directories
            case VANILLA:
                // defaultDirectories.add("some/vanillaspecific/path");
                // Check for no plugins and no mods folder
                filesAndFolders.addAll(cpp.getPathsRecursively("./" + worldName));
                break;
            case LIGHT_MODDED:
                // defaultDirectories.add("some/lightmoddedspecific/path");
                // Check for plugins and no mod folder
                break;
            case MODDED:
                // defaultDirectories.add("some/moddedspecific/path");
                // Check for mod folder
                break;
        }
        return filesAndFolders.toArray(new String[0]);
    }
}
