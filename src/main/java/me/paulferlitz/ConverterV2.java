package me.paulferlitz;

import me.paulferlitz.handlers.FileHandler;
import me.paulferlitz.handlers.UUIDHandler;
import me.paulferlitz.minecraftflavours.MinecraftFlavour;
import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Class for handling the file conversion.
 *
 * @author Paul Ferlitz
 */
public class ConverterV2
{
    // Class variables
    private final Path serverFolder;
    private final Path worldFolder;
    private final Map<UUID, Player> uuidMap = new HashMap<>();
    private final String[] ignoredFileExtensions = new String[]
            {
                    "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
                    "md", "snbt", "nbt", "nbt", "zip", "cache", "png", "jpeg", "js",
                    "DS_Store"
            };

    /**
     * Main constructor.
     *
     * @throws PathNotValidException if the path derived from server.properties or default could not be resolved.
     */
    public ConverterV2() throws PathNotValidException
    {
        this.serverFolder = Path.of("./");
        // Get path to world folder
        // TODO: Change method from using String to using Path - Needs exception for this case
        String worldName = FileHandler.readWorldNameFromProperties("./server.properties");
        this.worldFolder = this.serverFolder.resolve(worldName);
        if (!Files.exists(this.worldFolder))
        {
            throw new PathNotValidException(this.worldFolder.toAbsolutePath().toString());
        }
    }

    /**
     * Secondary constructor if the server folder path must be specified.
     *
     * @param serverFolderPath The path to the server's main folder.
     * @throws PathNotValidException if the path derived from server.properties or default could not be resolved.
     */
    public ConverterV2(String serverFolderPath) throws PathNotValidException
    {
        this.serverFolder = Path.of(serverFolderPath);
        // Get path to world folder
        Path serverProperties = this.serverFolder.resolve("server.properties");
        // TODO: Change method from using String to using Path
        String worldName = FileHandler.readWorldNameFromProperties(serverProperties.toString());
        this.worldFolder = this.serverFolder.resolve(worldName);
        if (Files.exists(this.worldFolder))
        {
            System.out.println("Working path set to \"" + this.serverFolder.toAbsolutePath() + "\"");
        } else
        {
            throw new PathNotValidException(this.worldFolder.toAbsolutePath().toString());
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
        Path usercache = this.serverFolder.resolve("usercache.json");
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(usercache.toString());
        String playerName = "N/A";
        uuidMap.clear();
        // Iterate over all players
        for (Object obj : knownPlayers)
        {
            JSONObject knownPlayer = (JSONObject) obj;
            try
            {
                // Try converting knownPlayer to a Player object by fetching or generating his UUID etc.
                Player player;
                UUID onlineUUID;
                switch (mode)
                {
                    case "offline":
                        // TODO: Make shorter by allowing direct String UUID input
                        onlineUUID = UUID.fromString(knownPlayer.getString("uuid"));
                        playerName = knownPlayer.getString("name");
                        UUID offlineUUID = UUIDHandler.onlineUUIDToOffline(onlineUUID);
                        player = new Player(playerName, offlineUUID);
                        break;
                    case "online":
                        playerName = knownPlayer.getString("name");
                        onlineUUID = UUIDHandler.onlineNameToUUID(playerName);
                        player = new Player(playerName, onlineUUID);
                        break;
                    default:
                        throw new InvalidArgumentException(mode);
                }
                // Save to the Hashmap for later
                uuidMap.put(UUID.fromString(knownPlayer.getString("uuid")), player);

                System.out.println("Was able to prefetch player " + playerName + " from usercache.json");
            } catch (IOException | JSONException e)
            {
                System.out.println("Wasn't able to prefetch player " + playerName +
                        " (" + knownPlayer.getString("uuid") + ") from usercache.json -> Probably an offline account!");
            }
        }
    }

    private boolean preCheck(String mode) throws InvalidArgumentException
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
            if (uuidMap.isEmpty())
            {
                System.out.println("\nCould not find any offline profiles that were convertable to online profiles. Aborting...");
                return false;
            }
        }
        return true;
    }

    /**
     * Main class method for converting all player bound files.
     * This method fetches a list of default directories, changing with the flavour of Minecraft server.
     * Then gets all files from those directories and converts them.
     *
     * @param mode If the server should be converted to offline or online mode.
     * @throws InvalidArgumentException if an illegal argument was detected.
     */
    public void convert(String mode, MinecraftFlavour flavour) throws InvalidArgumentException, IOException
    {
        if (!preCheck(mode)) return;

        String onlineMode = mode.equals("-offline") ? "false" : "true";
        FileHandler.writeToProperties(this.serverFolder.resolve("server.properties"), "online-mode", onlineMode);

        // Iterate over every flavour specific directory
        for (String str : flavour.getFiles(this.serverFolder.toString(), this.worldFolder.getFileName().toString()))
        {
            File currentFile = this.serverFolder.resolve(str).toFile();
            if (currentFile.isDirectory()) continue;

            boolean exclude = false;
            for (String ext : ignoredFileExtensions) {
                if (currentFile.getName().endsWith("." + ext)) {
                    exclude = true;
                    break;
                }
            }

            if (exclude) continue;

            System.out.println("\nWorking on \"" + str + "\"...");

            Path currentPath = currentFile.toPath();

            String fullFileName = currentPath.getFileName().toString();
            int dotIndex = fullFileName.lastIndexOf('.');
            String fileName, fileExtension;
            if (dotIndex != -1)
            {
                fileName = fullFileName.substring(0, dotIndex);
                fileExtension = fullFileName.substring(dotIndex);
            } else
            {
                fileName = fullFileName;
                fileExtension = "";
            }

            try
            {
                UUID currentUUID = UUID.fromString(fileName);
                if (!uuidMap.containsKey(currentUUID) && mode.equals("-offline"))
                {
                    //TODO: onlineUUIDToOffline utilises onlineUUIDToName -> make better to save on API requests
                    uuidMap.put(currentUUID, new Player(UUIDHandler.onlineUUIDToName(currentUUID),  UUIDHandler.onlineUUIDToOffline(currentUUID)));
                }
                Path temp = Paths.get(uuidMap.get(currentUUID).getUuid().toString() + fileExtension);
                Path newFileName = this.serverFolder.resolve(str).getParent().resolve(temp);
                //System.out.println(file);
                //System.out.println(newFileName);
                Files.move(currentPath, newFileName, StandardCopyOption.REPLACE_EXISTING);
                //continue;
            } catch (IllegalArgumentException | IOException | JSONException e)
            {
                if (e instanceof IOException)
                {
                    // This error should only happen if you intentionally tinker whilst the application is running, but I still am handling it.
                    System.out.println("\n!!! FILE CONFLICT !!!");
                    System.out.println("Source file: " + currentPath.toAbsolutePath());
                    System.out.println("Please resolve on your own!");
                }
            }

            if(!Files.isRegularFile(currentPath)) continue;
            if (!FileHandler.isText(currentPath)) continue;

            try
            {
                String content = Files.readString(currentPath);
                for (Map.Entry<UUID, Player> entry : uuidMap.entrySet())
                {
                    content = content.replaceAll(entry.getKey().toString(), entry.getValue().getUuid().toString());
                }
                Files.writeString(currentPath, content);
            } catch (IOException e)
            {
                // This error should only happen if you intentionally tinker whilst the application is running, but I still am handling it.
                System.out.println("\n!!! FILE CONFLICT !!!");
                System.out.println("Source file: " + currentPath.toAbsolutePath());
                System.out.println("Please resolve on your own!");
            }
        }
    }
}
