package me.pauleff.converter;

import java.nio.file.Path;
import java.util.List;

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

    // TODO 27.05.2026 - For now the method returns mostly the full world folders. This works fine but should be optimized later.
    public List<Path> dimensionRootFolders(Path serverFolder, Path worldFolder)
    {
        return switch (this)
        {
            case SINGLE -> List.of(worldFolder);
            case PER_DIMENSION -> List.of(
                    worldFolder,
                    serverFolder.resolve(worldFolder.getFileName() + "_nether"),
                    serverFolder.resolve(worldFolder.getFileName() + "_the_end")
            );
            case SINGLE_2026 -> List.of(
                    worldFolder.resolve("dimensions/minecraft/overworld"),
                    worldFolder.resolve("dimensions/minecraft/the_nether"),
                    worldFolder.resolve("dimensions/minecraft/the_end"),
                    worldFolder.resolve("players")
            );
        };
    }
}
