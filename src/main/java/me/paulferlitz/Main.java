package me.paulferlitz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.lang.System.exit;

public class Main
{
    private static String pathToWorld = "";
    private static final String[] workingDirs = new String[]{"playerdata", "advancements", "stats"};
    private static String mode = "N/A";
    private static boolean hasPath = false;

    public static void main(String[] args) throws IOException
    {
        long startTime = System.nanoTime();
        for (int i = 0; i < args.length; i++)
        {
            switch (args[i].toLowerCase(Locale.ROOT))
            {
                case "-p":
                    hasPath = true;
                    pathToWorld = args[i + 1];
                    break;
                case "-offline":
                case "-online":
                    mode = args[i];
                    break;
                default:
                    if (!hasPath)
                        System.out.println("Error occured, wrong argument. Use -online or -offline and if neede -p with path/to/world/folder!");
            }
        }

        if (!hasPath)
        {
            System.out.println("Set default working path (\"./world/\")");
            pathToWorld = "./world/";
        } else
        {
            if (!pathToWorld.endsWith("/")) pathToWorld = pathToWorld + "/";
            System.out.println("Working path set to \"" + pathToWorld + "\"");
        }

        boolean doesFolderExist = Files.exists(Path.of(pathToWorld));

        if (!doesFolderExist) System.out.println("Path to world folder \""+ pathToWorld+"\" does not exist!");

        if (!mode.equals("N/A") && doesFolderExist)
        {
            UUIDHandler uuidHandler = new UUIDHandler();
            JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(pathToWorld + "../usercache.json");
            switch (mode)
            {
                case "-offline":
                    Map<UUID, OfflinePlayer> uuidMap = new HashMap<>();

                    for (Object obj : knownPlayers)
                    {
                        JSONObject knownPlayer = (JSONObject) obj;
                        try
                        {
                            uuidMap.put(
                                    UUID.fromString(knownPlayer.getString("uuid")),
                                    new OfflinePlayer(
                                            knownPlayer.getString("name"),
                                            UUID.fromString(uuidHandler.onlineUUIDToOffline(knownPlayer.getString("uuid")))));
                            System.out.println("Was able to prefetch player " + knownPlayer.getString("name") + " from usercache.json");
                        } catch (NullPointerException e)
                        {
                            System.out.println("Wasn't able to prefetch player " +
                                    knownPlayer.getString("name") + " (" + knownPlayer.getString("uuid") +
                                    ") from usercache.json. Probably offline!");
                        }

                    }

                    for (String workingDir : workingDirs)
                    {
                        System.out.println("Working on " + workingDir + "...");
                        String pathToWorkingDir = pathToWorld + workingDir + "/";
                        FileHandler fh = new FileHandler();
                        File fileList[] = fh.listAllFiles(pathToWorkingDir);
                        if (fileList == null) continue;
                        for (File file : fileList)
                        {
                            if (file.isFile() && !file.getName().endsWith("old"))
                            {
                                String currentFile = file.getName();
                                try
                                {
                                    String currentFileName;
                                    if (workingDir.equals("playerdata"))
                                    {
                                        currentFileName = currentFile.substring(0, currentFile.length() - 4);
                                        if (!uuidMap.containsKey(UUID.fromString(currentFileName)))
                                        {
                                            uuidMap.put(
                                                    UUID.fromString(currentFileName),
                                                    new OfflinePlayer(
                                                            uuidHandler.onlineUUIDToName(currentFileName),
                                                            UUID.fromString(uuidHandler.onlineUUIDToOffline(currentFileName))));
                                        }
                                        fh.renameFile(pathToWorkingDir, currentFile, uuidMap.get(UUID.fromString(currentFileName)).getUuid() + ".dat");
                                    } else
                                    {
                                        currentFileName = currentFile.substring(0, currentFile.length() - 5);
                                        if (!uuidMap.containsKey(UUID.fromString(currentFileName)))
                                        {
                                            uuidMap.put(
                                                    UUID.fromString(currentFileName),
                                                    new OfflinePlayer(
                                                            uuidHandler.onlineUUIDToName(currentFileName),
                                                            UUID.fromString(uuidHandler.onlineUUIDToOffline(currentFileName))));
                                        }
                                        fh.renameFile(pathToWorkingDir, currentFile, uuidMap.get(UUID.fromString(currentFileName)).getUuid() + ".json");
                                    }
                                    System.out.println("Player " + uuidMap.get(UUID.fromString(currentFileName)).getName() + " --> " +
                                            currentFileName + " to " + uuidMap.get(UUID.fromString(currentFileName)).getUuid());
                                } catch (NullPointerException | IllegalArgumentException | FileAlreadyExistsException e)
                                {
                                    if(e instanceof NullPointerException)
                                    {
                                        System.out.println("There was a problem whilst converting the UUID from the file " +
                                                pathToWorkingDir + currentFile + ". Probably offline!");
                                    }else if(e instanceof IllegalArgumentException)
                                    {
                                        // For now do nothing
                                    }else if(e instanceof FileAlreadyExistsException)
                                    {
                                        System.out.println("\n!!! FILE CONFLICT !!!");
                                        System.out.println("Source file: " + pathToWorkingDir + currentFile);
                                        System.out.println("Output file: " + ((FileAlreadyExistsException) e).getFile());
                                        System.out.println("Please resolve on your own!\n");
                                    }
                                }
                            }

                        }
                    }
                    break;
                case "-online":
                    System.out.println("Converting from offline to online is currently WIP!");
                    break;
                default:
                    System.out.println("There was a fatal error!");
            }
        }
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        if (args.length > 0)
        {
            System.out.println("\nJob finished in " + (timeElapsed / 1000000) + " milliseconds.");
        } else
        {
            System.out.println("Error occured wrong argument. Use -online or -offline and if neede -p with path/to/world/folder!");
        }
    }
}
