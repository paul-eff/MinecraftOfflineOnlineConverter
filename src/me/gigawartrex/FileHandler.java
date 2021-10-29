package me.gigawartrex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler
{
    public static boolean renameFile(String baseWorldFolder, String oldFilePath, String newFilePath)
    {
        Path source = Paths.get(baseWorldFolder + oldFilePath);
        Path target = Paths.get(baseWorldFolder + newFilePath);
        try
        {
            Files.move(source, target);
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static File[] listAllFiles(String worldFolderpath)
    {
        File folder = new File(worldFolderpath);
        return folder.listFiles();
    }
}
