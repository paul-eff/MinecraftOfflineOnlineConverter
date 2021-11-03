package me.paulferlitz;

import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Converter
{
    private final String[] workingDirs = new String[]{"playerdata", "advancements", "stats"};
    private final String worldFolderPath;

    private UUIDHandler uuidHandler = new UUIDHandler();
    private Map<UUID, Player> uuidMap = new HashMap<>();

    public Converter() throws PathNotValidException
    {
        this.worldFolderPath = "./world/";
        if (Files.exists(Path.of(this.worldFolderPath)))
        {
            System.out.println("Using default working path (\"./world/\")");
        } else
        {
            throw new PathNotValidException(this.worldFolderPath);
        }
    }

    public Converter(String worldFolderPath) throws PathNotValidException
    {
        this.worldFolderPath = worldFolderPath.endsWith("/") ? worldFolderPath : (worldFolderPath + "/");
        if (Files.exists(Path.of(this.worldFolderPath)))
        {
            System.out.println("Working path set to \"" + this.worldFolderPath + "\"");
        } else
        {
            throw new PathNotValidException(this.worldFolderPath);
        }
    }

    private void fetchUsercache(String mode) throws InvalidArgumentException
    {
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(this.worldFolderPath + "../usercache.json");
        uuidMap.clear();

        for (Object obj : knownPlayers)
        {
            JSONObject knownPlayer = (JSONObject) obj;
            try
            {
                Player player;
                switch (mode)
                {
                    case "offline":
                        player = new Player(knownPlayer.getString("name"),
                                uuidHandler.onlineUUIDToOffline(UUID.fromString(knownPlayer.getString("uuid"))));
                        break;
                    case "online":
                        player = new Player(knownPlayer.getString("name"),
                                uuidHandler.onlineNameToUUID(knownPlayer.getString("name")));
                        break;
                    default:
                        throw new InvalidArgumentException(mode);
                }
                uuidMap.put(UUID.fromString(knownPlayer.getString("uuid")), player);

                System.out.println("Was able to prefetch player " + knownPlayer.getString("name") + " from usercache.json");
            } catch (NullPointerException | IOException e)
            {
                System.out.println("Wasn't able to prefetch player " + knownPlayer.getString("name") +
                        " (" + knownPlayer.getString("uuid") + ") from usercache.json. Probably offline!");
            }
        }
    }

    public boolean convert(String mode) throws InvalidArgumentException
    {
        if (mode.equals("-offline"))
        {
            System.out.println("CONVERSION: ONLINE --> OFFLINE");
            fetchUsercache("offline");
        } else
        {
            System.out.println("CONVERSION: OFFLINE --> ONLINE");
            fetchUsercache("online");
        }

        for (String workingDir : this.workingDirs)
        {
            System.out.println("Working on " + workingDir + "...");

            String pathToWorkingDir = this.worldFolderPath + workingDir + "/";
            File fileList[] = FileHandler.listAllFiles(pathToWorkingDir);

            if (fileList == null) continue;
            for (File file : fileList)
            {
                if (file.isFile() && (file.getName().endsWith(".dat") || file.getName().endsWith(".json")))
                {
                    String currentFile = file.getName();
                    String fileEnding = file.getName().endsWith(".dat") ? ".dat" : ".json";
                    String currentFileName = currentFile.substring(0, currentFile.length() - fileEnding.length());

                    UUID currentUUID = UUID.fromString(currentFileName);

                    try
                    {
                        if (!uuidMap.containsKey(currentUUID) && mode.equals("-offline"))
                        {
                            //TODO: onlineUUIDToOffline utilises onlineUUIDToName -> make better to save on API requests
                            uuidMap.put(UUID.fromString(currentFileName),
                                    new Player(UUIDHandler.onlineUUIDToName(UUID.fromString(currentFileName)),
                                            UUIDHandler.onlineUUIDToOffline(UUID.fromString(currentFileName))));
                        }
                        FileHandler.renameFile(pathToWorkingDir, currentFile, uuidMap.get(currentUUID).getUuid() + fileEnding);

                        Path path = Paths.get(this.worldFolderPath + "../usercache.json");
                        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                        content = content.replaceAll(currentFileName, uuidMap.get(UUID.fromString(currentFileName)).getUuid().toString());
                        Files.write(path, content.getBytes(StandardCharsets.UTF_8));

                        System.out.println("Player " + uuidMap.get(UUID.fromString(currentFileName)).getName() + " --> " +
                                currentFileName + " to " + uuidMap.get(UUID.fromString(currentFileName)).getUuid());
                    } catch (IOException | NullPointerException e)
                    {
                        if (e instanceof NullPointerException)
                        {
                            System.out.println("There was a problem whilst converting the UUID from the file " +
                                    pathToWorkingDir + currentFile + ". Probably offline!");
                        } else if (e instanceof FileAlreadyExistsException)
                        {
                            System.out.println("\n!!! FILE CONFLICT !!!");
                            System.out.println("Source file: " + pathToWorkingDir + currentFile);
                            System.out.println("Output file: " + ((FileAlreadyExistsException) e).getFile());
                            System.out.println("Please resolve on your own!\n");
                        } else
                        {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
