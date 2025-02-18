package me.paulferlitz.handlers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Handles UUID-related actions, including conversions between online and offline UUIDs.
 *
 * Uses Mojang's API for retrieving UUIDs and names from online services.
 *
 * @author Paul Ferlitz
 */
public class UUIDHandler {
    private static final Logger logger = LoggerFactory.getLogger(UUIDHandler.class);
    private static final HTTPHandler http = new HTTPHandler();

    /**
     * Converts a player name to an offline UUID.
     *
     * @param offlineName The player's name.
     * @return The resulting {@link UUID}.
     */
    public static UUID offlineNameToUUID(String offlineName) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
        logger.info("Generated offline UUID for player '{}': {}", offlineName, uuid);
        return uuid;
    }

    /**
     * Converts a player name to an online UUID by querying Mojang's API.
     *
     * @param onlineName The player's name.
     * @return The resulting {@link UUID}.
     * @throws IOException If a connection issue occurs.
     */
    public static UUID onlineNameToUUID(String onlineName) throws IOException {
        http.setUrl("https://api.mojang.com/users/profiles/minecraft/" + onlineName);
        String response = http.httpDoGet();

        if (response == null || response.isEmpty()) {
            logger.warn("No UUID found for online player '{}'.", onlineName);
            return null;
        }

        JSONObject json = new JSONObject(response);
        String uuid = json.optString("id", "").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        if (uuid.isEmpty()) {
            logger.warn("Invalid UUID retrieved for '{}'.", onlineName);
            return null;
        }

        logger.info("Retrieved online UUID for player '{}': {}", onlineName, uuid);
        return UUID.fromString(uuid);
    }

    /**
     * Retrieves a player's name from their online {@link UUID} using Mojang's API.
     *
     * @param onlineUUID The player's {@link UUID}.
     * @return The player's name, or null if not found.
     * @throws IOException If a connection issue occurs.
     */
    public static String onlineUUIDToName(UUID onlineUUID) throws IOException {
        http.setUrl("https://api.mojang.com/user/profile/" + onlineUUID.toString());
        String response = http.httpDoGet();

        if (response == null || response.isEmpty()) {
            logger.warn("No name found for UUID '{}'.", onlineUUID);
            return null;
        }

        JSONObject json = new JSONObject(response);
        String name = json.optString("name", "");

        if (name.isEmpty()) {
            logger.warn("Invalid name retrieved for UUID '{}'.", onlineUUID);
            return null;
        }

        logger.info("Retrieved player name for UUID '{}': {}", onlineUUID, name);
        return name;
    }

    /**
     * Converts an online UUID to an offline UUID by first retrieving the player's name.
     *
     * @param onlineUUID The player's online {@link UUID}.
     * @return The player's offline {@link UUID}, or null if conversion fails.
     * @throws IOException If a connection issue occurs.
     */
    public static UUID onlineUUIDToOffline(UUID onlineUUID) throws IOException {
        String playerName = onlineUUIDToName(onlineUUID);
        if (playerName == null) {
            logger.error("Failed to convert online UUID '{}' to an offline UUID due to missing name.", onlineUUID);
            return null;
        }
        return offlineNameToUUID(playerName);
    }
}
