package me.paulferlitz;

import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler
{
    public static void renameFile(String baseWorldFolder, String oldFilePath, String newFilePath) throws IOException
    {
        Path source = Paths.get(baseWorldFolder + oldFilePath);
        Path target = Paths.get(baseWorldFolder + newFilePath);
        Files.move(source, target);
    }

    public static File[] listAllFiles(String worldFolderpath)
    {
        File folder = new File(worldFolderpath);
        return folder.listFiles();
    }

    public static JSONArray loadArrayFromUsercache(String pathToUsercache)
    {
        String jsonString = "[]";
        try
        {
            jsonString = Files.readString(Path.of(pathToUsercache), StandardCharsets.UTF_8);
        } catch (IOException e)
        {
            System.out.println("Could not find usercache.json with given path \"" + pathToUsercache + "\"." +
                    "\nContinuing without prefetching userdata.");
        }
        return new JSONArray(jsonString);
    }

    public static String readWorldNameFromProperties(String pathToProperties)
    {
        boolean foundInProperties = false;
        String worldName = "world";

        try (BufferedReader br = new BufferedReader(new FileReader(pathToProperties))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("level-name="))
                {
                    worldName = line.replace("level-name=", "");
                    System.out.println("Found world name \"" + worldName + "\" in server.properties. Trying to target this world folder.");
                    foundInProperties = true;
                }
            }
        } catch (IOException e)
        {
            System.out.println("Could not find server.properties with given path \"" + pathToProperties + "\"." +
                    "\nContinuing without prefetching world name.");
        }

        if(!foundInProperties)
        {
            System.out.println("No world name found. Using default (\"" + worldName + "\").");
        }

        return worldName;
    }
}
