package me.paulferlitz;

import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Class for handling file operations.
 *
 * @author Paul Ferlitz
 */
public class FileHandler
{
    /**
     * Method to rename a file.
     *
     * @param baseWorldFolder Base working directory.
     * @param oldFilePath Old path / name for file in question.
     * @param newFilePath New path / name for file in question.
     * @throws IOException If renaming wasn't possible.
     */
    public static void renameFile(String baseWorldFolder, String oldFilePath, String newFilePath) throws IOException
    {
        Path source = Paths.get(baseWorldFolder + oldFilePath);
        Path target = Paths.get(baseWorldFolder + newFilePath);
        Files.move(source, target);
    }

    /**
     * Method to list all files in a given directory.
     *
     * @param worldFolderpath Path to folder.
     * @return An array of {@link File}s.
     */
    public static File[] listAllFiles(String worldFolderpath)
    {
        File folder = new File(worldFolderpath);
        return folder.listFiles();
    }

    /**
     * Method to load the array of players in the usercache.json file.
     *
     * @param pathToUsercache Path to usercache.json file.
     * @return The usercache.json content as a {@link JSONArray}.
     */
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

    /**
     * Method to fetch the world name from the server.properties files.
     *
     * @param pathToProperties Path to server.properties file.
     * @return The world name.
     */
    public static String readWorldNameFromProperties(String pathToProperties)
    {
        boolean foundInProperties = false;
        // Default value
        String worldName = "world";
        // Iterate over file until 'level-name' tag was found
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
