package me.paulferlitz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            Map<UUID, Player> uuidMap = new HashMap<>();
            switch (mode)
            {
                case "-offline":

                    for (Object obj : knownPlayers)
                    {
                        JSONObject knownPlayer = (JSONObject) obj;
                        try
                        {
                            uuidMap.put(
                                    UUID.fromString(knownPlayer.getString("uuid")),
                                    new Player(
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
                                                    new Player(
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
                                                    new Player(
                                                            uuidHandler.onlineUUIDToName(currentFileName),
                                                            UUID.fromString(uuidHandler.onlineUUIDToOffline(currentFileName))));
                                        }
                                        fh.renameFile(pathToWorkingDir, currentFile, uuidMap.get(UUID.fromString(currentFileName)).getUuid() + ".json");
                                    }

                                    Path path = Paths.get(pathToWorld + "../usercache.json");
                                    Charset charset = StandardCharsets.UTF_8;
                                    String content = new String(Files.readAllBytes(path), charset);
                                    content = content.replaceAll(currentFileName, uuidMap.get(UUID.fromString(currentFileName)).getUuid().toString());
                                    Files.write(path, content.getBytes(charset));

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
                    /**
                     * If you are reading this part of my code: DON'T!!!!!
                     * You really don't want to see all the copy pasta and the lazy shit I did!
                     * Just please don't!
                     * I will fix this later, I promise! Promise made on 02.11.2021 at 4 p.m.!
                     */
                    for (Object obj : knownPlayers)
                    {
                        JSONObject knownPlayer = (JSONObject) obj;
                        try
                        {
                            uuidMap.put(
                                    UUID.fromString(knownPlayer.getString("uuid")),
                                    new Player(
                                            knownPlayer.getString("name"),
                                            uuidHandler.onlineNameToUUID(knownPlayer.getString("name"))));
                            System.out.println("Was able to prefetch player " + knownPlayer.getString("name") + " from usercache.json");
                        } catch (NullPointerException e)
                        {
                            System.out.println("Wasn't able to prefetch player " +
                                    knownPlayer.getString("name") + " from usercache.json. Probably a pure offline account!");
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
                                        if (uuidMap.containsKey(UUID.fromString(currentFileName)))
                                        {
                                            fh.renameFile(pathToWorkingDir, currentFile, uuidMap.get(UUID.fromString(currentFileName)).getUuid() + ".dat");
                                        }
                                    } else
                                    {
                                        currentFileName = currentFile.substring(0, currentFile.length() - 5);
                                        if (uuidMap.containsKey(UUID.fromString(currentFileName)))
                                        {
                                            fh.renameFile(pathToWorkingDir, currentFile, uuidMap.get(UUID.fromString(currentFileName)).getUuid() + ".json");
                                        }
                                    }

                                    Path path = Paths.get(pathToWorld + "../usercache.json");
                                    Charset charset = StandardCharsets.UTF_8;
                                    String content = new String(Files.readAllBytes(path), charset);
                                    content = content.replaceAll(currentFileName, uuidMap.get(UUID.fromString(currentFileName)).getUuid().toString());
                                    Files.write(path, content.getBytes(charset));

                                    System.out.println("Player " + uuidMap.get(UUID.fromString(currentFileName)).getName() + " --> " +
                                            currentFileName + " to " + uuidMap.get(UUID.fromString(currentFileName)).getUuid());
                                } catch (IllegalArgumentException | FileAlreadyExistsException e)
                                {
                                    if(e instanceof IllegalArgumentException)
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
                default:
                    System.out.println("There was a critical error!");
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
