package me.paulferlitz.MinecraftFlavours;

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

    public String[] getDirectories(MinecraftFlavour flavour, String worldName) throws Exception
    {
        String[] defaultDirectories = new String[]{
                "./",
                "./" + worldName + "/playerdata",
                "./" + worldName + "/advancements",
                "./" + worldName + "/stats"
        };
        switch(flavour)
        {
            case VANILLA:
                return defaultDirectories;
            case LIGHT_MODDED:
                return defaultDirectories;
            case MODDED:
                return defaultDirectories;
            default:
                throw new Exception("MinecraftFlavours type \""+flavour+"\" not valid!");
        }
    }
}
