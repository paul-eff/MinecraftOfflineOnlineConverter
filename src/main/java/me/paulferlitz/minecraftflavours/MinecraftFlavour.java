package me.paulferlitz.minecraftflavours;

import me.paulferlitz.handlers.CustomPathParser;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    public String[] getDirectories(String worldName)
    {
        ArrayList<String> defaultDirectories = new ArrayList<>();
        defaultDirectories.add("./");
        defaultDirectories.add("./" + worldName + "/playerdata");
        defaultDirectories.add("./" + worldName + "/advancements");
        defaultDirectories.add("./" + worldName + "/stats");

        CustomPathParser cpp = new CustomPathParser();
        ArrayList<String> pathList = cpp.getPaths();
        if(!pathList.isEmpty()) defaultDirectories.addAll(pathList);

        switch(this)
        {
            // TODO: Find flavour specific directories
            case VANILLA:
                // defaultDirectories.add("some/vanillaspecific/path");
                break;
            case LIGHT_MODDED:
                // defaultDirectories.add("some/lightmoddedspecific/path");
                break;
            case MODDED:
                // defaultDirectories.add("some/moddedspecific/path");
                break;
        }
        return defaultDirectories.toArray(new String[0]);
    }
}
