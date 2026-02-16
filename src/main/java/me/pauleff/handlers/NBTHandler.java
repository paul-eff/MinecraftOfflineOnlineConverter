package me.pauleff.handlers;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Path;

public class NBTHandler {

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

                // Tags to preserve (Position, Rotation, Dimension, Spawn points)
                String[] tagsToPreserve = {
                        "Pos", "Rotation", "Dimension",
                        "SpawnX", "SpawnY", "SpawnZ", "SpawnForced"
                };

                for (String key : tagsToPreserve) {
                    if (destCompound.containsKey(key)) {
                        sourceCompound.put(key, destCompound.get(key));
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
}
