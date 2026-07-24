package me.pauleff.common.handlers;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides utilities for reading, writing, and inspecting Minecraft NBT files.
 */
public final class NBTHandler
{
    private static final String[] DEFAULT_TAGS_TO_KEEP = {
            "Pos",
            "Rotation",
            "Dimension",
            //Flying, and gamemode data
            //"abilities.flying",
            //"abilities.mayfly",
            //"abilities.invulnerable",
            //"playerGameType",
            //Spawn Data
            "SpawnX",
            "SpawnY",
            "SpawnZ",
            "SpawnDimension",
            "SpawnForced"
    };

    private NBTHandler()
    {
    }

    /**
     * Determines whether a file can be parsed as a valid NBT document.
     * <p>
     * Attempts to read the file via {@link NBTUtil#read(File)}, which detects GZIP
     * and NBT headers. Parse failures (including invalid gzip or tag IDs) are treated
     * as non-NBT.
     *
     * @param file the file to inspect
     * @return {@code true} if the file parses as NBT; {@code false} otherwise
     */
    public static boolean isNBTFile(File file)
    {
        try
        {
            // NBTUtil.read tries to detect GZIP and valid NBT headers.
            // If it fails to parse the root tag, it's likely not NBT.
            NBTUtil.read(file);
            return true;
        } catch (IOException e)
        {
            // This includes "Not in GZIP format" or "Invalid Tag ID"
            return false;
        }
    }

    /**
     * Writes playerdata from {@code nbtSource} to {@code nbtDest}, preserving selected
     * tags from the destination when it already exists.
     * <p>
     * Loads the source compound, then for each path in the default keep-list copies the
     * corresponding tag from the destination into the source before writing. Nested paths
     * use dot notation (e.g. {@code abilities.flying}). The destination is written with
     * GZIP compression as handled by the Querz NBT library.
     *
     * @param nbtSource the path to the source NBT playerdata file
     * @param nbtDest   the path to write the merged playerdata to
     * @throws IOException if reading or writing either NBT file fails
     */
    public static void copyPlayerDataNBT(Path nbtSource, Path nbtDest) throws IOException
    {
        // 1. Load the new data we want to apply (Source)
        NamedTag sourceRoot = NBTUtil.read(nbtSource.toFile());
        CompoundTag sourceCompound = (CompoundTag) sourceRoot.getTag();
        // 2. If the destination exists, grab the tags we want to keep
        if (Files.exists(nbtDest))
        {
            NamedTag destRoot = NBTUtil.read(nbtDest.toFile());
            CompoundTag destCompound = (CompoundTag) destRoot.getTag();
            //We use dot notation to specify nested tags
            for (String tagPath : DEFAULT_TAGS_TO_KEEP)
            {
                preserveTag(sourceCompound, destCompound, tagPath);
            }
        }
        // 3. Write the modified source compound to the destination path
        // Querz NBT handles GZIP compression automatically by default
        NBTUtil.write(sourceCompound, nbtDest.toFile());
    }

    /**
     * Copies a tag from {@code dest} into {@code source} at the given dotted path.
     * <p>
     * Intermediate compound tags missing on the source are created as needed. If any
     * segment of the path is absent on the destination, the method returns without changes.
     *
     * @param source the compound receiving the preserved tag
     * @param dest   the compound providing the tag to keep
     * @param path   the dotted tag path (e.g. {@code SpawnX} or {@code abilities.flying})
     */
    private static void preserveTag(CompoundTag source, CompoundTag dest, String path)
    {
        String[] parts = path.split("\\.");
        CompoundTag currentSource = source;
        CompoundTag currentDest = dest;

        // Traverse to the parent of the target tag
        for (int i = 0; i < parts.length - 1; i++)
        {
            String part = parts[i];
            if (currentDest.containsKey(part))
            {
                // If source is missing the 'folder', create it so we have somewhere to put the leaf
                if (!currentSource.containsKey(part))
                {
                    currentSource.put(part, new CompoundTag());
                }
                currentDest = currentDest.getCompoundTag(part);
                currentSource = currentSource.getCompoundTag(part);
            } else
            {
                // Target data doesn't exist in destination, nothing to preserve
                return;
            }
        }
        // Copy the actual leaf tag (the last part of the path)
        String leaf = parts[parts.length - 1];
        if (currentDest.containsKey(leaf))
        {
            currentSource.put(leaf, currentDest.get(leaf));
        }
    }
}
