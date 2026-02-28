package me.pauleff.handlers;

import me.pauleff.Main;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NBTHandler {

    static String[] DEFAULT_TAGS_TO_KEEP = {
            //position
            "Pos",
            "Rotation",
            "Dimension",
            //Flying, and gamemode data
//            "abilities.flying",
//            "abilities.mayfly",
//            "abilities.invulnerable",
//            "playerGameType",
            //Spawn Data
            "SpawnX",
            "SpawnY",
            "SpawnZ",
            "SpawnDimension",
            "SpawnForced"
    };

    public static boolean isNBTFile(File file) {
        try {
            // NBTUtil.read tries to detect GZIP and valid NBT headers.
            // If it fails to parse the root tag, it's likely not NBT.
            NBTUtil.read(file);
            return true;
        } catch (IOException e) {
            // This includes "Not in GZIP format" or "Invalid Tag ID"
            return false;
        }
    }

    public static void copyPlayerDataNBT(Path nbtSource, Path nbtDest) {
        try {
            // 1. Load the new data we want to apply (Source)
            NamedTag sourceRoot = NBTUtil.read(nbtSource.toFile());
            CompoundTag sourceCompound = (CompoundTag) sourceRoot.getTag();

            // 2. If the destination exists, grab the tags we want to keep
            if (Files.exists(nbtDest)) {
                NamedTag destRoot = NBTUtil.read(nbtDest.toFile());
                CompoundTag destCompound = (CompoundTag) destRoot.getTag();

                //We use dot notation to specify nested tags
                if (!Main.config.playerDataTagsToKeep.isEmpty()) {
                    for (String path : Main.config.playerDataTagsToKeep) {
                        preserveTag(sourceCompound, destCompound, path);
                    }
                } else {
                    for (String path : DEFAULT_TAGS_TO_KEEP) {
                        preserveTag(sourceCompound, destCompound, path);
                    }
                }
            }

            // 3. Write the modified source compound to the destination path
            // Querz NBT handles GZIP compression automatically by default
            NBTUtil.write(sourceCompound, nbtDest.toFile());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void preserveTag(CompoundTag source, CompoundTag dest, String path) {
        String[] parts = path.split("\\.");
        CompoundTag currentSource = source;
        CompoundTag currentDest = dest;

        // Traverse to the parent of the target tag
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (currentDest.containsKey(part)) {
                // If source is missing the 'folder', create it so we have somewhere to put the leaf
                if (!currentSource.containsKey(part)) {
                    currentSource.put(part, new CompoundTag());
                }
                currentDest = currentDest.getCompoundTag(part);
                currentSource = currentSource.getCompoundTag(part);
            } else {
                return; // Target data doesn't exist in destination, nothing to preserve
            }
        }

        // Copy the actual leaf tag (the last part of the path)
        String leaf = parts[parts.length - 1];
        if (currentDest.containsKey(leaf)) {
            currentSource.put(leaf, currentDest.get(leaf));
        }
    }
}
