package me.pauleff.converter;

import java.nio.file.Path;
import java.util.List;

/**
 * Describes how a Minecraft world's dimensions are laid out under the server folder.
 * <p>
 * Each constant carries a human-readable description returned by {@link #toString()}.
 * {@link #dimensionRootFolders(Path, Path)} resolves the concrete dimension roots for a
 * given server and world folder.
 */
public enum WorldFolderStructure
{
    /**
     * Standard Vanilla layout: a single world folder containing the overworld plus
     * {@code DIM1} and {@code DIM-1}.
     */
    SINGLE("Standard Vanilla structure, single folder containing world, DIM1 and DIM-1"),

    /**
     * Bukkit-style layout with separate {@code world}, {@code world_nether}, and
     * {@code world_the_end} folders beside the server root.
     */
    PER_DIMENSION("World structure often used by Bukkit type server where world, world_nether and world_the_end exist"),

    /**
     * Vanilla layout introduced in 2026.1 with world data under a {@code dimensions} subdirectory.
     */
    SINGLE_2026("New Vanilla structure introduced in 2026.1 with world data saved to the dimensions subdirectory");

    private final String description;

    /**
     * Creates a world folder structure with the given human-readable description.
     *
     * @param description the description shown by {@link #toString()}
     */
    WorldFolderStructure(String description)
    {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this world folder structure.
     *
     * @return the description associated with this constant
     */
    @Override
    public String toString()
    {
        return description;
    }

    // TODO 27.05.2026 - For now the method returns mostly the full world folders. This works fine but should be optimized later.

    /**
     * Resolves the dimension root folders for this structure under the given server and world paths.
     * <p>
     * The returned paths are the roots that conversion plugins walk for player and world files.
     * For {@link #PER_DIMENSION}, sibling nether and end folders are derived from the world
     * folder name. For {@link #SINGLE_2026}, dimension and players subpaths under the world
     * folder are returned.
     *
     * @param serverFolder the server root folder
     * @param worldFolder  the primary world folder from {@code server.properties}
     * @return the dimension (and related) root folders to process; never {@code null}
     */
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
