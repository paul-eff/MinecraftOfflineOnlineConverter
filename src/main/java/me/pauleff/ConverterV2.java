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

    public final Path serverFolder;
    public Path worldFolder;
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
        this(Path.of("./").toAbsolutePath().normalize());
    }

    /**
     * Constructor allowing a custom server folder path.
     *
     * @param serverFolderPath The path to the Minecraft server directory.
     * @throws PathNotValidException if the world folder cannot be resolved.
     */
    public ConverterV2(Path serverFolderPath) throws PathNotValidException {
        this.serverFolder = serverFolderPath;
        LOGGER.info("Server folder set to: {}", this.serverFolder.toAbsolutePath().normalize());
    }

    public void setWorldFolder() throws PathNotValidException {
        Path serverProperties = this.serverFolder.resolve("server.properties");
        String worldName = FileHandler.readWorldNameFromProperties(serverProperties);
        this.worldFolder = this.serverFolder.resolve(worldName);
        if (!Files.exists(this.worldFolder)) {
            throw new PathNotValidException(this.worldFolder.toAbsolutePath().normalize());
        }
    }

    /**
     * Fetches all known players from usercache.json and stores them in a map.
     *
     * @param toOnlineMode Specifies if the server should be converted to offline or online mode.
     */
    private void fetchUsercache(boolean toOnlineMode) {
        Path usercache = this.serverFolder.resolve("usercache.json");
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(usercache);

        uuidMap.clear();

        for (Object obj : knownPlayers) {
            JSONObject knownPlayer = (JSONObject) obj;
            try {
                String playerName = knownPlayer.getString("name");
                UUID playerUUID = UUID.fromString(knownPlayer.getString("uuid")); //Can be online or offline!

                if (toOnlineMode) {
                    UUID onlineUUID = UUIDHandler.onlineNameToUUID(playerName);//Converts a player name to an online UUID by querying Mojang's API.
                    uuidMap.put(playerUUID, new Player(playerName, onlineUUID));
                    LOGGER.info("Prefetched player: {} ({})", playerName, onlineUUID);
                } else {
                    //Since we only want offline UUID, we can just retrieve it from player name
                    UUID offlineUUID = UUIDHandler.offlineNameToUUID(playerName);

                    uuidMap.put(playerUUID, new Player(playerName, offlineUUID));
                    LOGGER.info("Prefetched player: {} ({})", playerName, offlineUUID);
                }


            } catch (IOException | JSONException e) {
                LOGGER.warn("Could not prefetch player from usercache.json", e);
            }
        }
    }

    /**
     * Performs a pre-check before conversion to ensure valid player data is available.
     *
     * @param toOnlineMode Conversion mode ("-online" or "-offline").
     * @return True if conversion can proceed, false otherwise.
     */
    private boolean preCheck(boolean toOnlineMode) {
        if (!toOnlineMode) {
            LOGGER.info("CONVERSION: ONLINE --> OFFLINE");
        } else {
            LOGGER.info("CONVERSION: OFFLINE --> ONLINE");
        }
        fetchUsercache(toOnlineMode);

        if (toOnlineMode && uuidMap.isEmpty()) {
            LOGGER.error("No offline profiles found to convert to online profiles. Aborting...");
            return false;
        }
        return true;
    }

    /**
     * Copies all player data from the source world to the destination world
     *
     * @param sourceWorld The source world
     * @param flavor      The Minecraft server type, defining file locations.
     * @throws IOException if file operations fail.
     */
    public void copyPlayerData(String sourceWorld, MinecraftFlavor flavor) throws IOException {
        Path relativeSource = Paths.get(sourceWorld);
        if (relativeSource.isAbsolute()) {
            relativeSource = relativeSource.getRoot().relativize(relativeSource);
        }

        Path sourceWorldFolder = this.serverFolder.resolve(relativeSource).toAbsolutePath().normalize();
        Path destWorldFolder = this.worldFolder;
        if (!destWorldFolder.toFile().exists() || Files.isSameFile(sourceWorldFolder, destWorldFolder)) {
            LOGGER.warn("Could not move player data from {} to {}. Destination folder is invalid",
                    this.serverFolder.relativize(sourceWorldFolder),
                    this.serverFolder.relativize(destWorldFolder));
            return;
        }

        LOGGER.info("Copying player data from {} to {}",
                this.serverFolder.relativize(sourceWorldFolder),
                this.serverFolder.relativize(destWorldFolder));

        String[] allFiles = flavor.getFiles(this.serverFolder, this.serverFolder.relativize(sourceWorldFolder), true);
        int discoveredValidFiles = 0;
        int movedFiles = 0;
        for (String relativePath : allFiles) {
            Path currentPath = this.serverFolder.resolve(relativePath).normalize();
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/")) {
                continue;
            }

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith)) {
                continue;
            }
            LOGGER.info("Processing file: {}", currentPath);

            try {
                //Move all files from playerdata no matter what
                boolean inPlayerdata = currentPath.getParent().getFileName().toString().equals("playerdata");
                //Check if this is a valid UUID filename before moving
                if (!inPlayerdata) {
                    String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());
                    if (!UUIDHandler.isValidUUID(fileName)) {
                        continue;
                    }
                }

                discoveredValidFiles++;

                // 1. Get the "tail" (world/region/data.mca)
                Path tail = sourceWorldFolder.relativize(currentPath);
                // 2. Attach it to the new base
                Path finalPath = destWorldFolder.resolve(tail);
                LOGGER.info("Copying file to {}", destWorldFolder.normalize());
//                FileHandler.renameFile(currentPath, finalPath.toAbsolutePath().normalize().toString());
                Files.copy(currentPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                movedFiles++;
            } catch (IllegalArgumentException | IOException e) {
                // TODO: Message was not very helpful, maybe add more details/context later?
                LOGGER.debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
            }
        }

        LOGGER.info("Copied {} out of {} files to {}", movedFiles, discoveredValidFiles, destWorldFolder.normalize().toString());
    }

    /**
     * Converts all player-related files and UUIDs to match the selected mode.
     *
     * @param toOnlineMode Conversion mode ("-online" or "-offline").
     * @param flavor       The Minecraft server type, defining file locations.
     * @throws IOException if file operations fail.
     */
    public void convert(boolean toOnlineMode, MinecraftFlavor flavor) throws IOException {
        if (!preCheck(toOnlineMode)) return;

        FileHandler.writeToProperties(this.serverFolder.resolve("server.properties"), "online-mode", Boolean.toString(toOnlineMode));

        String[] allFiles = flavor.getFiles(this.serverFolder, this.serverFolder.relativize(this.worldFolder), false);
        int discoveredValidFiles = 0;
        int renamedFiles = 0;
        for (String relativePath : allFiles) {
            Path currentPath = this.serverFolder.resolve(relativePath).normalize();
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/")) {
                continue;
            }

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith)) {
                continue;
            }

            LOGGER.info("Processing file: {}", currentPath);

            try {
                String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());

                if (UUIDHandler.isValidUUID(fileName)) {
                    UUID fileUUID = UUID.fromString(fileName);//Could be online or offline!
                    UUIDHandler.UUIDType uuidType = UUIDHandler.getUUIDType(fileUUID);
                    discoveredValidFiles++;
                    LOGGER.info("File UUID Type: {}", uuidType);

                    if (toOnlineMode) {
                        if (uuidType == UUIDHandler.UUIDType.ONLINE) continue;
                        //We have no way of knowing the player name from just on offline UUID, so we have to skip it
                    } else {
                        if (uuidType == UUIDHandler.UUIDType.OFFLINE) continue;
                        if (!uuidMap.containsKey(fileUUID)) {
                            String playerName = UUIDHandler.onlineUUIDToName(fileUUID);
                            UUID onlineUUID = UUIDHandler.offlineNameToUUID(playerName);
                            uuidMap.put(fileUUID, new Player(playerName, onlineUUID));
                        }
                    }

                    if (uuidMap.get(fileUUID) == null) {//if usercache.json has no record of the player, and we couldn't extract it from the file name, there is nothing we can do
                        LOGGER.warn("Unable to fetch player data from file. Skipping...");
                        continue;
                    }
                    //Rename the file to online or offline UUID
                    LOGGER.info("Renaming file to {}", uuidMap.get(fileUUID).getTargetUUID().toString());
                    FileHandler.renameFile(currentPath, uuidMap.get(fileUUID).getTargetUUID().toString());
                    renamedFiles++;
                }
            } catch (IllegalArgumentException | IOException e) {
                // TODO: Message was not very helpful, maybe add more details/context later?
                LOGGER.debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
            }

            if (Files.isRegularFile(currentPath)) {
                if (FileHandler.isText(currentPath)) {
                    String content = Files.readString(currentPath);
                    boolean didReplace = false;
                    for (Map.Entry<UUID, Player> entry : uuidMap.entrySet()) {
                        if (!didReplace) didReplace = content.contains(entry.getKey().toString());
                        content = content.replace(entry.getKey().toString(), entry.getValue().getTargetUUID().toString());
                    }
                    if (didReplace) {
                        Files.writeString(currentPath, content);
                        LOGGER.info("Updated UUIDs in file: {}", currentPath.normalize());
                    }
                } else if (currentPath.toString().contains("entities")) {
                    //Try reading as NBT or Anvil
                    // TODO: Implement NBT/Anvil file handling
                }
            }

        }


        LOGGER.info("Changed {} out of {} files to {} UUIDs", renamedFiles, discoveredValidFiles, toOnlineMode ? "online" : "offline");
    }
}
