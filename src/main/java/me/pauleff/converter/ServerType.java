package me.pauleff.converter;

import me.pauleff.Main;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public enum ServerType
{
    VANILLA("Default vanilla Minecraft server"),
    BUKKIT("Server-side extensions via plugins (Bukkit,Paper,...)"),
    MODDED("Server- & client-side extensions via mods (Forge,Fabric,...)");

    private final String description;

    ServerType(String description)
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
            case VANILLA -> filesAndFolders.addAll(Main.config.getPathsRecursively("./" + worldDirectory));
            case BUKKIT, MODDED ->
            {
            }
        }
        return filesAndFolders.toArray(String[]::new);
    }
}
