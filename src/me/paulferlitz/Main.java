package me.paulferlitz;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
                        System.out.println("Error occured wrong argument. Use -online or -offline and if neede -p with path/to/world/folder!");
            }
        }
        if (!mode.equals("N/A"))
        {
            UUIDHandler uuidHandler = new UUIDHandler();
            if (!hasPath)
            {
                System.out.println("Set default working path (\"./world/\")");
                pathToWorld = "./world/";
            } else
            {
                if (!pathToWorld.endsWith("/")) pathToWorld = pathToWorld + "/";
                System.out.println("Working path set to \"" + pathToWorld + "\"");
            }
            switch (mode)
            {
                case "-offline":
                    Map<UUID, OfflinePlayer> uuidMap = new HashMap<>();
                    for (String workingDir : workingDirs)
                    {
                        String pathToWorkingDir = pathToWorld + workingDir + "/";
                        FileHandler fh = new FileHandler();
                        for (File file : fh.listAllFiles(pathToWorkingDir))
                        {
                            if (file.isFile() && !file.getName().endsWith("old"))
                            {
                                String currentFile = file.getName();
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
            System.out.println("Job finished in " + (timeElapsed / 1000000) + " milliseconds.");
        } else
        {
            System.out.println("Error occured wrong argument. Use -online or -offline and if neede -p with path/to/world/folder!");
        }
    }
}
