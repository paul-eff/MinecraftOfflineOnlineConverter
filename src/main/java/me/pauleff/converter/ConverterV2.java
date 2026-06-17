package me.pauleff.converter;

import me.pauleff.Main;
import me.pauleff.common.exceptions.PathNotValidException;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.NBTHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.detection.MinecraftFlavor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static me.pauleff.common.handlers.UUIDHandler.getUUIDType;
import static me.pauleff.converter.UUIDType.OFFLINE;
import static me.pauleff.converter.UUIDType.ONLINE;

public class ConverterV2
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterV2.class);
    private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of(
            "mcr", "mca", "jar", "gz", "lock", "sh", "bat", "log", "mcmeta",
            "md", "snbt", "nbt", "zip", "cache", "png", "jpeg", "js", "DS_Store"
    );
    public final Path serverFolder;
    public final Path serverProperties;
    private final Map<UUID, Player> uuidMap = new HashMap<>();
    public Path worldFolder;
    private int lastUsercacheEntryCount;

    public ConverterV2() throws PathNotValidException
    {
        this(Path.of("./").toAbsolutePath().normalize());
    }

    public ConverterV2(Path serverFolderPath) throws PathNotValidException
    {
        this.serverFolder = serverFolderPath;

        if (!Files.exists(this.serverFolder))
        {
            throw new PathNotValidException("Server folder not found", this.serverFolder.toAbsolutePath().normalize());
        } else
        {
            LOGGER.info("Server folder set to: {}", this.serverFolder.toAbsolutePath().normalize());
        }

        serverProperties = this.serverFolder.resolve("server.properties");
        if (!Files.exists(this.serverProperties))
        {
            throw new PathNotValidException("Could not find server.properties", this.serverProperties.toAbsolutePath().normalize());
        }
    }

    public void setWorldFolder(String worldName) throws PathNotValidException
    {
        this.worldFolder = this.serverFolder.resolve(worldName);
        if (!Files.exists(this.worldFolder))
        {
            throw new PathNotValidException(this.worldFolder.toAbsolutePath().normalize());
        }
    }

    private void fetchUsercache(boolean toOnlineMode)
    {
        Path usercache = this.serverFolder.resolve("usercache.json");
        JSONArray knownPlayers = FileHandler.loadArrayFromUsercache(usercache);

        uuidMap.clear();
        lastUsercacheEntryCount = knownPlayers.length();

        for (Object obj : knownPlayers)
        {
            JSONObject knownPlayer = (JSONObject) obj;
            try
            {
                String playerName = knownPlayer.getString("name");
                UUID playerUUID = UUID.fromString(knownPlayer.getString("uuid")); //Can be online or offline!

                if (toOnlineMode)
                {
                    UUID onlineUUID = UUIDHandler.nameToOnlineUUID(playerName);//Converts a player name to an online UUID by querying Mojang's API.
                    if (onlineUUID == null)
                    {
                        LOGGER.warn("Skipping player '{}' — no online UUID found.", playerName);
                        continue;
                    }
                    uuidMap.put(playerUUID, new Player(playerName, onlineUUID));
                    LOGGER.info("Prefetched player: {} ({})", playerName, onlineUUID);
                } else
                {
                    //Since we only want offline UUID, we can just retrieve it from player name
                    UUID offlineUUID = UUIDHandler.nameToOfflineUUID(playerName);

                    uuidMap.put(playerUUID, new Player(playerName, offlineUUID));
                    LOGGER.info("Prefetched player: {} ({})", playerName, offlineUUID);
                }


            } catch (IOException | JSONException e)
            {
                LOGGER.warn("Could not prefetch player from usercache.json", e);
            }
        }
    }

    private boolean preCheck(boolean toOnlineMode)
    {
        if (!toOnlineMode)
        {
            LOGGER.info("CONVERSION: ONLINE --> OFFLINE");
        } else
        {
            LOGGER.info("CONVERSION: OFFLINE --> ONLINE");
        }
        fetchUsercache(toOnlineMode);

        if (toOnlineMode && uuidMap.isEmpty())
        {
            if (lastUsercacheEntryCount > 0)
            {
                LOGGER.error("No online profile could be resolved for any player in usercache.json (all lookups failed). Aborting...");
            } else
            {
                LOGGER.error("No offline profiles found to convert to online profiles. Aborting...");
            }
            return false;
        }
        return true;
    }

    public void copyPlayerData(String sourceWorld, MinecraftFlavor flavor) throws IOException
    {
        Path _relativeSource = Paths.get(sourceWorld);
        if (_relativeSource.isAbsolute())
        {
            _relativeSource = _relativeSource.getRoot().relativize(_relativeSource);
        }
        Path sourceWorldFolder = this.serverFolder.resolve(_relativeSource).toAbsolutePath().normalize();
        Path destWorldFolder = this.worldFolder;

        Path relativeSourceWorldFolder = this.serverFolder.relativize(sourceWorldFolder);
        Path relativeDestWorldFolder = this.serverFolder.relativize(destWorldFolder);

        if (!destWorldFolder.toFile().exists() || Files.isSameFile(sourceWorldFolder, destWorldFolder))
        {
            LOGGER.warn("Could not move player data from {} to {}. Destination folder is invalid", relativeSourceWorldFolder, relativeDestWorldFolder);
            return;
        }
//        System.out.println(Main.config.playerdataWorldBlacklist);
        if (Main.config.playerdataWorldBlacklist.contains(relativeSourceWorldFolder.toString()) ||
                Main.config.playerdataWorldBlacklist.contains(relativeDestWorldFolder.toString()))
        {
            LOGGER.warn("Could not move player data from {} to {}. Source or destination folder is blacklisted.", relativeSourceWorldFolder, relativeDestWorldFolder);
            return;
        }

        LOGGER.info("Copying player data from {} to {}", relativeSourceWorldFolder, relativeDestWorldFolder);

        String[] allFiles = flavor.getFiles(relativeSourceWorldFolder, true);
        int movedFiles = 0;
        for (String relativePath : allFiles)
        {
            Path currentPath = this.serverFolder.resolve(relativePath).normalize();
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/"))
            {
                continue;
            }

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith))
            {
                continue;
            }
            LOGGER.info("Processing file: {}", currentPath);
            try
            {
                Path tail = sourceWorldFolder.relativize(currentPath);
                Path finalPath = destWorldFolder.resolve(tail);

                if (currentPath.getParent().getFileName().toString().equals("playerdata"))
                {
                    if (NBTHandler.isNBTFile(currentFile))
                    {
                        LOGGER.info("Copying NBT file to {}", destWorldFolder.normalize());
                        NBTHandler.copyPlayerDataNBT(currentPath, finalPath);
                        movedFiles++;
                        continue;
                    }
                } else
                {
                    String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());
                    if (!UUIDHandler.isValidUUID(fileName)) continue;
                }

                LOGGER.info("Copying file to {}", destWorldFolder.normalize());
                Files.copy(currentPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                movedFiles++;
            } catch (IllegalArgumentException | IOException e)
            {
                // TODO: Message was not very helpful, maybe add more details/context later?
                LOGGER.debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
            }
        }

        LOGGER.info("Copied {} files to {}", movedFiles, destWorldFolder.normalize());
    }

    public void convert(boolean toOnlineMode, MinecraftFlavor flavor) throws IOException
    {
        if (!preCheck(toOnlineMode)) return;
        FileHandler.writeToProperties(serverProperties, "online-mode", Boolean.toString(toOnlineMode));

        String[] allFiles = flavor.getFiles(this.serverFolder.relativize(this.worldFolder), false);
        int discoveredValidFiles = 0;
        int renamedFiles = 0;
        for (String relativePath : allFiles)
        {
            Path currentPath = this.serverFolder.resolve(relativePath).normalize();
            File currentFile = currentPath.toFile();

            // TODO: IMPORTANT REMOVE AGAIN LATER - STILL WORKING ON MCA SUPPORT!!!!
            if (currentFile.toString().contains("/region/"))
            {
                continue;
            }

            if (!currentFile.isFile() || IGNORED_FILE_EXTENSIONS.stream().anyMatch(currentFile.getName()::endsWith))
            {
                continue;
            }

            LOGGER.info("Processing file: {}", currentPath);

            try
            {
                String fileName = FileHandler.stripFileExtension(currentPath.getFileName().toString());

                if (UUIDHandler.isValidUUID(fileName))
                {
                    UUID fileUUID = UUID.fromString(fileName);//Could be online or offline!
                    UUIDType sourceUuidType = getUUIDType(fileUUID);
                    discoveredValidFiles++;
                    LOGGER.info("File UUID Type: {}", sourceUuidType);

                    if (toOnlineMode)
                    {
                        if (sourceUuidType == ONLINE) continue;
                        //We have no way of knowing the player name from just on offline UUID, so we have to skip it
                    } else
                    {
                        if (sourceUuidType == OFFLINE) continue;
                        if (!uuidMap.containsKey(fileUUID))
                        {
                            String playerName = UUIDHandler.onlineUUIDToName(fileUUID);
                            UUID offlineUUID = UUIDHandler.nameToOfflineUUID(playerName);
                            uuidMap.put(fileUUID, new Player(playerName, offlineUUID));
                        }
                    }

                    if (uuidMap.get(fileUUID) == null)
                    {//if usercache.json has no record of the player, and we couldn't extract it from the file name, there is nothing we can do
                        LOGGER.warn("Unable to fetch player data from file. Skipping...");
                        continue;
                    }
                    //Rename the file to online or offline UUID
                    LOGGER.info("Renaming file to {}", uuidMap.get(fileUUID).newUUID().toString());
                    FileHandler.renameFile(currentPath, uuidMap.get(fileUUID).newUUID().toString());
                    renamedFiles++;
                }
            } catch (IllegalArgumentException | IOException e)
            {
                // TODO: Message was not very helpful, maybe add more details/context later?
                LOGGER.debug("Skipping file {} due to an error: {}", currentPath.normalize(), e.getMessage());
            }

            if (Files.isRegularFile(currentPath))
            {
                if (FileHandler.isTextBasedFile(currentPath))
                {
                    String content = Files.readString(currentPath);
                    boolean didReplace = false;
                    for (Map.Entry<UUID, Player> entry : uuidMap.entrySet())
                    {
                        if (!didReplace) didReplace = content.contains(entry.getKey().toString());
                        content = content.replace(entry.getKey().toString(), entry.getValue().newUUID().toString());
                    }
                    if (didReplace)
                    {
                        Files.writeString(currentPath, content);
                        LOGGER.info("Updated UUIDs in file: {}", currentPath.normalize());
                    }
                } else if (currentPath.toString().contains("entities"))
                {
                    //Try reading as NBT or Anvil
                    // TODO: Implement NBT/Anvil file handling
                }
            }

        }


        LOGGER.info("Changed {} out of {} files to {} UUIDs", renamedFiles, discoveredValidFiles, toOnlineMode ? "online" : "offline");
    }
}
