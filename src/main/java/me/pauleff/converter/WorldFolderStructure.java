package me.pauleff.converter;

/**
 * Enum representing different Minecraft world folder structures.
 * Needed, as the folder structure how the world and it's dimensions was saved has changed over the years.
 */
public enum WorldFolderStructure
{
    SINGLE("Standard Vanilla structure, single folder containing world, DIM1 and DIM-1"),
    PER_DIMENSION("World structure often used by Bukkit type server where world, world_nether and world_the_end exist"),
    SINGLE_2026("New Vanilla structure introduced in 2026.1 with world data saved to the dimensions subdirectory");

    private final String description;

    WorldFolderStructure(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
