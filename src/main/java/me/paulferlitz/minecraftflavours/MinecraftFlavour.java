package me.paulferlitz.minecraftflavours;

import java.util.ArrayList;
import java.util.Arrays;

public enum MinecraftFlavour
{
    VANILLA("Vanilla"),
    LIGHT_MODDED("Lightly Modded (Bukkit,Paper,...)"),
    MODDED("Modded (Forge,Fabric,...)");

    private final String description;

    private MinecraftFlavour(String description)
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
        ArrayList<String> defaultDirectories = new ArrayList<String>();
        defaultDirectories.add("./");
        defaultDirectories.add("./" + worldName + "/playerdata");
        defaultDirectories.add("./" + worldName + "/advancements");
        defaultDirectories.add("./" + worldName + "/stats");
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
