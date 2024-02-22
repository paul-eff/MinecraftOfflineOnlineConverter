package me.paulferlitz;

import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;
import me.paulferlitz.handlers.FileHandler;
import me.paulferlitz.handlers.UUIDHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class for handling the file conversion.
 *
 * @author Paul Ferlitz
 */
public class Converter
{
    // Class variables
    private final String[] workingDirs = new String[]{"playerdata", "advancements", "stats"};
    private final String[] workingFiles = new String[]{"whitelist.json", "ops.json", "usercache.json", "banned-players.json", "banned-ips.json"};
    private final String worldFolderPath;
    private UUIDHandler uuidHandler = new UUIDHandler();
    private Map<UUID, Player> uuidMap = new HashMap<>();

    /**
     * Main constructor.
     *
     * @throws PathNotValidException if the path derived from server.properties or default could not be resolved.
     */
    public Converter() throws PathNotValidException
    {
        // Get path to world folder
        this.worldFolderPath = "./" + FileHandler.readWorldNameFromProperties("./server.properties") + "/";
        if (!Files.exists(Path.of(this.worldFolderPath)))
        {
            throw new PathNotValidException(this.worldFolderPath);
        }
    }

    /**
     * Secondary constructor if the server folder path must be specified.
     *
     * @param serverFolderPath The path to the server's main folder.
     * @throws PathNotValidException if the path derived from server.properties or default could not be resolved.
     */
    public Converter(String serverFolderPath) throws PathNotValidException
    {
        // Cleanup and set server folder path
        serverFolderPath = serverFolderPath.endsWith("/") ? serverFolderPath : (serverFolderPath + "/");
        // Get path to world folder
        this.worldFolderPath = serverFolderPath + FileHandler.readWorldNameFromProperties(serverFolderPath + "server.properties") + "/";
        if (Files.exists(Path.of(this.worldFolderPath)))
        {
            System.out.println("Working path set to \"" + this.worldFolderPath + "\"");
        } else
        {
            throw new PathNotValidException(this.worldFolderPath);
        }
    }

    /**
     * Method that fetches all known players from the usercache.json file.
     * No return value, as it's stores in {@link #uuidMap}.
     *
     * @param mode If the server should be converted to offline or online mode.
     * @throws InvalidArgumentException if an illegal argument was detected.
     */
    private void fetchUsercache(String mode) throws InvalidArgumentException
    {
        // Get all players from usercache.json
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(this.worldFolderPath + "../usercache.json");
        uuidMap.clear();
        // Iterate over all players
        for (Object obj : knownPlayers)
        {
            JSONObject knownPlayer = (JSONObject) obj;
            try
            {
                // Try converting knownPlayer to a Player object by fetching or generating his UUID etc.
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
                // Save to the Hashmap for later
                uuidMap.put(UUID.fromString(knownPlayer.getString("uuid")), player);

                System.out.println("Was able to prefetch player " + knownPlayer.getString("name") + " from usercache.json");
            } catch (NullPointerException | IOException e)
            {
                System.out.println("Wasn't able to prefetch player " + knownPlayer.getString("name") +
                        " (" + knownPlayer.getString("uuid") + ") from usercache.json. Probably offline!");
            }
        }
    }

    /**
     * Main class method for converting all player bound files.
     *
     * @param mode If the server should be converted to offline or online mode.
     * @return True if conversion was successfull.
     * @throws InvalidArgumentException if an illegal argument was detected.
     */
    public boolean convert(String mode) throws InvalidArgumentException
    {
        if (!mode.equals("-online"))
        {
            System.out.println("\nCONVERSION: ONLINE --> OFFLINE");
            fetchUsercache("offline");
        } else
        {
            System.out.println("\nCONVERSION: OFFLINE --> ONLINE");
            fetchUsercache("online");
            // Abort process if all found players are offline only profiles (nothing to do here).
            if (uuidMap.size() <= 0)
            {
                System.out.println("\nCould not find any offline profiles that were covertable to online profiles. Aborting...");
                return false;
            }
        }
        // Iterate over every know directory with player files and convert it's content.
        for (String workingDir : this.workingDirs)
        {
            System.out.println("\nWorking on " + workingDir + "...");

            String pathToWorkingDir = this.worldFolderPath + workingDir + "/";
            File[] fileList = FileHandler.listAllFiles(pathToWorkingDir);

            if (fileList == null) continue;
            for (File file : fileList)
            {
                // Make sure to only convert current files (exclude _old etc.)
                if (file.isFile() && (file.getName().endsWith(".dat") || file.getName().endsWith(".json")))
                {
                    String currentFile = file.getName();
                    String fileEnding = file.getName().endsWith(".dat") ? ".dat" : ".json";
                    String currentFileName = currentFile.substring(0, currentFile.length() - fileEnding.length());

                    try
                    {
                        UUID currentUUID = UUID.fromString(currentFileName);

                        // If player not yet fetched from usercache.json, try to save him for later
                        if (!uuidMap.containsKey(currentUUID) && mode.equals("-offline"))
                        {
                            //TODO: onlineUUIDToOffline utilises onlineUUIDToName -> make better to save on API requests
                            uuidMap.put(UUID.fromString(currentFileName),
                                    new Player(UUIDHandler.onlineUUIDToName(UUID.fromString(currentFileName)),
                                            UUIDHandler.onlineUUIDToOffline(UUID.fromString(currentFileName))));
                        }
                        // Rename current file
                        FileHandler.renameFile(pathToWorkingDir, currentFile, uuidMap.get(currentUUID).getUuid() + fileEnding);
                        // Replace old entry in files holding old UUIDs to prevent clutter
                        for (String workingFile : this.workingFiles)
                        {
                            Path path = Paths.get(this.worldFolderPath + "../" + workingFile);
                            String content = Files.readString(path);
                            content = content.replaceAll(currentFileName, uuidMap.get(UUID.fromString(currentFileName)).getUuid().toString());
                            Files.writeString(path, content);
                        }

                        System.out.println("Player " + uuidMap.get(UUID.fromString(currentFileName)).getName() + " --> " +
                                currentFileName + " to " + uuidMap.get(UUID.fromString(currentFileName)).getUuid());
                    } catch (IOException | NullPointerException | IllegalArgumentException e)
                    {
                        if (e instanceof NullPointerException)
                        {
                            System.out.println("There was a problem whilst converting the UUID from the file " +
                                    pathToWorkingDir + currentFile + ". Probably offline!");
                        } else if (e instanceof FileAlreadyExistsException)
                        {
                            // This error should only happen if you intentionally tinker whilst the application is running, but I still am handling it.
                            System.out.println("\n!!! FILE CONFLICT !!!");
                            System.out.println("Source file: " + pathToWorkingDir + currentFile);
                            System.out.println("Output file: " + ((FileAlreadyExistsException) e).getFile());
                            System.out.println("Please resolve on your own!\n");
                        } else if (e instanceof IllegalArgumentException)
                        {
                            System.out.println("\n!!! UUID PROBLEM !!!");
                            System.out.println("Source file: " + pathToWorkingDir + currentFile);
                            System.out.println("Please report to the repository maintainer that there was a problem!\n");
                            System.out.println("Stacktrace:");
                            e.printStackTrace();
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
