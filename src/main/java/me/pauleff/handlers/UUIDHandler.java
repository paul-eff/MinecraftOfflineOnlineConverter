package me.pauleff.handlers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Handles UUID-related actions, including conversions between online and offline UUIDs.
 * Uses Mojang's API for retrieving UUIDs and names from online services.
 *
 * @author Paul Ferlitz
 */
public class UUIDHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHandler.class);
    private static final HTTPHandler HTTP = new HTTPHandler();
    private static final String apiBasePath = "https://api.mojang.com/";

    /**
     * Converts a player name to an offline UUID.
     * How to do this was documented <a href="https://www.spigotmc.org/threads/how-uuid-is-generated-for-offline-mode-nicknames.347835/">here</a> on the SpigotMC forums by user md_5.
     *
     * @param name The player's name.
     * @return The resulting {@link UUID}.
     */
    public static UUID offlineNameToUUID(String name)
    {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

    /**
     * Converts a player name to an online UUID by querying Mojang's API.
     *
     * @param name The player's name.
     * @return The resulting {@link UUID}.
     * @throws IOException If a connection issue occurs.
     */
    public static UUID onlineNameToUUID(String name) throws IOException
    {
        HTTP.setUrl(apiBasePath + "users/profiles/minecraft/" + name);
        String response = HTTP.httpDoGet();

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No UUID found for online player '{}'.", name);
            return null;
        }
        // Extract the UUID from the response
        JSONObject json = new JSONObject(response);
        String uuid = json.optString("id", "").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        // Check if the UUID is valid
        if (uuid.isEmpty())
        {
            LOGGER.warn("Invalid UUID retrieved for name '{}'.", name);
            return null;
        }

        LOGGER.info("Retrieved online UUID for player '{}': {}", name, uuid);
        return UUID.fromString(uuid);
    }

    /**
     * Retrieves a player's name from their online {@link UUID} using Mojang's API.
     *
     * @param uuid The player's {@link UUID}.
     * @return The player's name, or null if not found.
     * @throws IOException If a connection issue occurs.
     */
    public static String onlineUUIDToName(UUID uuid) throws IOException
    {
        HTTP.setUrl(apiBasePath + "/user/profile" + uuid.toString());
        String response = HTTP.httpDoGet();

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No name found for UUID '{}'.", uuid);
            return null;
        }
        // Extract the name from the response
        JSONObject json = new JSONObject(response);
        String name = json.optString("name", "");
        // Check if the name is valid
        if (name.isEmpty())
        {
            LOGGER.warn("Invalid name retrieved for UUID '{}'.", uuid);
            return null;
        }

        LOGGER.info("Retrieved player name for UUID '{}': {}", uuid, name);
        return name;
    }

    /**
     * Converts an online UUID to an offline UUID by first retrieving the player's name.
     *
     * @param onlineUUID The player's online {@link UUID}.
     * @return The player's offline {@link UUID}, or null if conversion fails.
     * @throws IOException If a connection issue occurs.
     */
    public static UUID onlineUUIDToOffline(UUID onlineUUID) throws IOException
    {
        String playerName = onlineUUIDToName(onlineUUID);
        if (playerName == null)
        {
            LOGGER.error("Failed to convert online UUID '{}' to offline (no name found).", onlineUUID);
            return null;
        }
        return offlineNameToUUID(playerName);
    }
}
