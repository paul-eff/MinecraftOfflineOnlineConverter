package me.pauleff;

import me.pauleff.handlers.FileHandler;
import me.pauleff.handlers.UUIDHandler;
import me.pauleff.minecraftflavors.MinecraftFlavor;
import me.pauleff.exceptions.PathNotValidException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Handles the conversion of Minecraft server player data between online and offline modes.
 * This includes renaming files and replacing UUIDs in various server files.
 *
 * @author Paul Ferlitz
 */
public class ConverterV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterV2.class);

    private final Path serverFolder;
    private final Path worldFolder;
    private final Map<UUID, Player> uuidMap = new HashMap<>();
    private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of(
            "mcr", "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
            "md", "snbt", "nbt", "zip", "cache", "png", "jpeg", "js", "DS_Store"
    );

    /**
     * Default constructor using the current directory as the server folder.
     *
     * @throws PathNotValidException if the world folder cannot be resolved.
     */
    public ConverterV2() throws PathNotValidException {
        this(Path.of("./"));
    }

    /**
     * Constructor allowing a custom server folder path.
     *
     * @param serverFolderPath The path to the Minecraft server directory.
     * @throws PathNotValidException if the world folder cannot be resolved.
     */
    public ConverterV2(Path serverFolderPath) throws PathNotValidException {
        this.serverFolder = serverFolderPath;
        Path serverProperties = this.serverFolder.resolve("server.properties");

        String worldName = FileHandler.readWorldNameFromProperties(String.valueOf(serverProperties));
        this.worldFolder = this.serverFolder.resolve(worldName);

        if (!Files.exists(this.worldFolder)) {
            throw new PathNotValidException(this.worldFolder.toAbsolutePath().toString());
        }

        LOGGER.info("Server folder set to: {}", this.serverFolder.toAbsolutePath());
    }

    /**
     * Fetches all known players from usercache.json and stores them in a map.
     *
     * @param mode Specifies if the server should be converted to offline or online mode.
     */
    private void fetchUsercache(String mode) {
        Path usercache = this.serverFolder.resolve("usercache.json");
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(String.valueOf(usercache));

        uuidMap.clear();

        for (Object obj : knownPlayers) {
            JSONObject knownPlayer = (JSONObject) obj;
            try {
                String playerName = knownPlayer.getString("name");
                UUID onlineUUID = UUID.fromString(knownPlayer.getString("uuid"));
                UUID convertedUUID = ("offline".equals(mode)) ? UUIDHandler.onlineUUIDToOffline(onlineUUID) : UUIDHandler.onlineNameToUUID(playerName);

                uuidMap.put(onlineUUID, new Player(playerName, convertedUUID));
                LOGGER.info("Prefetched player: {} ({})", playerName, convertedUUID);
            } catch (IOException | JSONException e) {
                LOGGER.warn("Could not prefetch player from usercache.json", e);
            }
        }
    }

    /**
     * Performs a pre-check before conversion to ensure valid player data is available.
     *
     * @param mode Conversion mode ("-online" or "-offline").
     * @return True if conversion can proceed, false otherwise.
     */
    private boolean preCheck(String mode) {
        boolean isOnline = "-online".equals(mode);
        if (!isOnline)
        {
            System.out.println("\nCONVERSION: ONLINE --> OFFLINE");
        } else
        {
            System.out.println("\nCONVERSION: OFFLINE --> ONLINE");
        }
        fetchUsercache(isOnline ? "online" : "offline");

        if (isOnline && uuidMap.isEmpty()) {
            LOGGER.error("No offline profiles found to convert to online profiles. Aborting...");
            return false;
        }
        return true;
    }

    /**
     * Converts all player-related files and UUIDs to match the selected mode.
     *
     * @param mode    Conversion mode ("-online" or "-offline").
     * @param flavor The Minecraft server type, defining file locations.
     * @throws IOException if file operations fail.
     */
    public void convert(String mode, MinecraftFlavor flavor) throws IOException {
        if (!preCheck(mode)) return;

        String onlineMode = Boolean.toString(!mode.equals("-offline"));
        FileHandler.writeToProperties(this.serverFolder.resolve("server.properties"), "online-mode", onlineMode);

        for (String relativePath : flavor.getFiles(this.serverFolder.toString(), this.worldFolder.getFileName().toString())) {
            Path currentPath = this.serverFolder.resolve(relativePath);
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/")){continue;}

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith)) {
                continue;
            }

            LOGGER.info("Processing file: {}", currentPath);

            try {
                String fileName = currentPath.getFileName().toString();
                UUID fileUUID = UUID.fromString(fileName.substring(0, fileName.lastIndexOf('.')));

                if (!uuidMap.containsKey(fileUUID) && "-offline".equals(mode)) {
                    uuidMap.put(fileUUID, new Player(UUIDHandler.onlineUUIDToName(fileUUID), UUIDHandler.onlineUUIDToOffline(fileUUID)));
                }

                // TODO: Currently a dirty fix. Should be handled correctly!
                if (uuidMap.get(fileUUID) == null) {
                    LOGGER.warn("Was not able to fetch information for file: {}", currentPath);
                    LOGGER.warn("Please send this error to the developer, along with some information on what you were converting to!");
                    LOGGER.warn("Skipping and continuing with rest of the files.");
                    LOGGER.warn("This can just be a temporary error that won't have any consequences, but please report it anyway.");
                    continue;
                }

                Path newFileName = currentPath.resolveSibling(uuidMap.get(fileUUID).getUuid() + fileName.substring(fileName.lastIndexOf('.')));
                Files.move(currentPath, newFileName, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Renamed {} to {}", currentPath, newFileName);

            } catch (IllegalArgumentException | IOException e) {
                // TODO: Message was not very helpful, maybe add more details/context later?
                if (Main.getArgs().hasOption("v")) LOGGER.warn("Skipping file {} due to an error: {}", currentPath, e.getMessage());
            }

            if (Files.isRegularFile(currentPath)) {
                if(FileHandler.isText(currentPath))
                {
                    String content = Files.readString(currentPath);
                    for (Map.Entry<UUID, Player> entry : uuidMap.entrySet()) {
                        content = content.replace(entry.getKey().toString(), entry.getValue().getUuid().toString());
                    }
                    Files.writeString(currentPath, content);
                    LOGGER.info("Updated UUIDs in file: {}", currentPath);
                }else if (currentPath.toString().contains("entities")){
                    //Try reading as NBT or Anvil
                    // TODO: Implement NBT/Anvil file handling
                }
            }
        }
    }
}
