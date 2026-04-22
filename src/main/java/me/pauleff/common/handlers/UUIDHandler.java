package me.pauleff.common.handlers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Handles UUID-related actions, including conversions between online and offline UUIDs.
 * Uses Mojang's API for retrieving UUIDs and names from online services.
 */
public class UUIDHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHandler.class);
    private static final HTTPHandler HTTP = new HTTPHandler();
    private static final String DEFAULT_NAME_API_BASE = "https://api.mojang.com/";
    private static final String DEFAULT_UUID_API_BASE = "https://api.minecraftservices.com/";
    private static String customApiBaseUrl = null;
    private static String retrieveUUIDUrl = null;
    private static String retrieveNameUrl = null;

    /**
     * Sets a custom API base URL (domain only). Mojang-style paths and the name/UUID
     * are appended when resolving. Overridden per-endpoint by
     * {@link #setRetrieveUUIDUrl(String)} / {@link #setRetrieveNameUrl(String)}.
     *
     * @param url The custom API base URL, or null to reset to defaults.
     */
    public static void setCustomApiBaseUrl(String url)
    {
        customApiBaseUrl = normalizeApiUrl(url);
        if (customApiBaseUrl != null)
        {
            LOGGER.info("Using custom base URL for online UUID/player name resolution: {}", customApiBaseUrl);
        }
    }

    /**
     * Sets the full endpoint URL (domain + path) for name→UUID lookups.
     * Only the player name is appended. When set, overrides {@link #setCustomApiBaseUrl(String)}.
     *
     * @param url The retrieve-UUID endpoint URL, or null to clear.
     */
    public static void setRetrieveUUIDUrl(String url)
    {
        retrieveUUIDUrl = normalizeApiUrl(url);
        if (retrieveUUIDUrl != null)
        {
            LOGGER.info("Using custom URL for retrieving online UUIDs: {}", retrieveUUIDUrl);
        }
    }

    /**
     * Sets the full endpoint URL (domain + path) for UUID→name lookups.
     * Only the UUID is appended. When set, overrides {@link #setCustomApiBaseUrl(String)}.
     *
     * @param url The retrieve-name endpoint URL, or null to clear.
     */
    public static void setRetrieveNameUrl(String url)
    {
        retrieveNameUrl = normalizeApiUrl(url);
        if (retrieveNameUrl != null)
        {
            LOGGER.info("Using custom URL for retrieving online player names: {}", retrieveNameUrl);
        }
    }

    private static String normalizeApiUrl(String url)
    {
        if (url == null || url.isBlank())
        {
            return null;
        }
        return url.endsWith("/") ? url : url + "/";
    }

    /**
     * Builds the name→UUID request URL.
     * {@code retrieveUUIDUrl} is a full endpoint (append name only);
     * {@code customApiBaseUrl} is a base (append Mojang path + name).
     */
    private static String buildNameToUuidUrl(String name)
    {
        if (retrieveUUIDUrl != null)
        {
            return retrieveUUIDUrl + name;
        }
        String base = customApiBaseUrl != null ? customApiBaseUrl : DEFAULT_NAME_API_BASE;
        return base + "users/profiles/minecraft/" + name;
    }

    /**
     * Builds the UUID→name request URL.
     * {@code retrieveNameUrl} is a full endpoint (append UUID only);
     * {@code customApiBaseUrl} is a base (append Mojang path + UUID).
     */
    private static String buildUuidToNameUrl(UUID uuid)
    {
        if (retrieveNameUrl != null)
        {
            return retrieveNameUrl + uuid.toString();
        }
        String base = customApiBaseUrl != null ? customApiBaseUrl : DEFAULT_UUID_API_BASE;
        return base + "minecraft/profile/lookup/" + uuid.toString();
    }

    /**
     * Converts a player name to an offline UUID.
     * How to do this was documented <a href="https://www.spigotmc.org/threads/how-uuid-is-generated-for-offline-mode-nicknames.347835/">here</a> on the SpigotMC forums by user md_5.
     *
     * @param name The player's name.
     * @return The resulting {@link UUID}.
     */
    public static UUID nameToOfflineUUID(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

    /**
     * Converts a player name to an online UUID by querying Mojang's API
     * (or a custom API if configured via {@link #setRetrieveUUIDUrl(String)} / {@link #setCustomApiBaseUrl(String)}).
     *
     * @param name The player's name.
     * @return The resulting {@link UUID}.
     * @throws IOException If a connection issue occurs.
     */
    public static UUID nameToOnlineUUID(String name) throws IOException {
        HTTP.set(buildNameToUuidUrl(name));
        String response = HTTP.get();

        if (response == null || response.isEmpty()) {
            LOGGER.warn("No UUID found for online player '{}'.", name);
            return null;
        }
        // Extract the UUID from the response
        JSONObject json = new JSONObject(response);
        String uuid = json.optString("id", "").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        // Check if the UUID is valid
        if (uuid.isEmpty()) {
            LOGGER.warn("Invalid UUID retrieved for name '{}'.", name);
            return null;
        }

        LOGGER.info("Retrieved online UUID for player '{}': {}", name, uuid);
        return UUID.fromString(uuid);
    }

    /**
     * Retrieves a player's name from their online {@link UUID} using Mojang's API
     * (or a custom API if configured via {@link #setRetrieveNameUrl(String)} / {@link #setCustomApiBaseUrl(String)}).
     *
     * @param uuid The player's {@link UUID}.
     * @return The player's name, or null if the player is not found/offline.
     * @throws IOException If a connection or rate-limiting issue occurs.
     */
    public static String onlineUUIDToName(UUID uuid) throws IOException {
        HTTP.set(buildUuidToNameUrl(uuid));
        String response = HTTP.get();

        // 2. Check for empty response
        if (response == null || response.isEmpty()) {
            LOGGER.warn("No profile found for UUID '{}'. This may be an offline/cracked UUID.", uuid);
            return null;
        }

        try {
            JSONObject json = new JSONObject(response);

            // 3. Extract the name field
            String name = json.optString("name", "");

            if (name.isEmpty()) {
                LOGGER.warn("Response for UUID '{}' did not contain a name.", uuid);
                return null;
            }

            LOGGER.info("Successfully retrieved name: {} for UUID: {}", name, uuid);
            return name;

        } catch (Exception e) {
            LOGGER.error("Failed to parse Mojang API response for UUID: {}", uuid, e);
            return null;
        }
    }

    public enum UUIDType {
        ONLINE,   // Version 4
        OFFLINE,  // Version 3
        INVALID   // Anything else
    }

    /**
     * Determines if a given {@link UUID} is online or offline
     * or offline (generated by the server).
     *
     * @param uuid The {@link UUID} to check.
     * @return True if the {@link UUID} is online, false otherwise.
     */
    public static UUIDType getUUIDType(UUID uuid) {
        int v = uuid.version();
        if (v == 4) return UUIDType.ONLINE;
        if (v == 3) return UUIDType.OFFLINE;
        return UUIDType.INVALID;
    }

    public static boolean isValidUUID(String uuidString) {
        if (uuidString == null || uuidString.length() != 36) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(uuidString);
            return getUUIDType(uuid) != UUIDType.INVALID;
        } catch (IllegalArgumentException e) {
            // This catches BOTH "Invalid UUID string" and "NumberFormatException"
            return false;
        }
    }
}
