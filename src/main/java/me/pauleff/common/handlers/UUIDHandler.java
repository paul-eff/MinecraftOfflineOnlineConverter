package me.pauleff.common.handlers;

import me.pauleff.converter.UUIDType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static me.pauleff.converter.UUIDType.*;

/**
 * Provides utilities for resolving Minecraft player names and UUIDs in online and offline mode.
 * <p>
 * Supports Mojang/Minecraft Services API lookups, optional custom API base URLs, and
 * classification of UUIDs by version (online vs offline).
 */
public final class UUIDHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHandler.class);
    private static final String DEFAULT_NAME_API_BASE = "https://api.mojang.com/";
    private static final String DEFAULT_UUID_API_BASE = "https://api.minecraftservices.com/";
    private static String customApiBaseUrl = null;
    private static String retrieveUUIDUrl = null;
    private static String retrieveNameUrl = null;

    /**
     * Sets a custom base URL used for both name-to-UUID and UUID-to-name lookups when
     * endpoint-specific URLs are not configured.
     *
     * @param url the custom API base URL, or {@code null}/blank to clear it
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
     * Sets a custom endpoint URL prefix for retrieving an online UUID from a player name.
     * <p>
     * When set, this overrides {@link #setCustomApiBaseUrl(String)} and the default Mojang endpoint
     * for name-to-UUID requests. The player name is appended to the given URL.
     *
     * @param url the custom retrieve-UUID URL prefix, or {@code null}/blank to clear it
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
     * Sets a custom endpoint URL prefix for retrieving a player name from an online UUID.
     * <p>
     * When set, this overrides {@link #setCustomApiBaseUrl(String)} and the default Minecraft Services
     * endpoint for UUID-to-name requests. The UUID string is appended to the given URL.
     *
     * @param url the custom retrieve-name URL prefix, or {@code null}/blank to clear it
     */
    public static void setRetrieveNameUrl(String url)
    {
        retrieveNameUrl = normalizeApiUrl(url);
        if (retrieveNameUrl != null)
        {
            LOGGER.info("Using custom URL for retrieving online player names: {}", retrieveNameUrl);
        }
    }

    /**
     * Normalizes an API URL by treating blank values as unset and ensuring a trailing slash.
     *
     * @param url the URL to normalize
     * @return the normalized URL ending with {@code /}, or {@code null} if {@code url} is {@code null} or blank
     */
    private static String normalizeApiUrl(String url)
    {
        if (url == null || url.isBlank())
        {
            return null;
        }
        return url.endsWith("/") ? url : url + "/";
    }

    /**
     * Builds the request URL used to resolve a player name to an online UUID.
     *
     * @param name the Minecraft player name
     * @return the fully constructed request URL
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
     * Builds the request URL used to resolve an online UUID to a player name.
     *
     * @param uuid the Minecraft player UUID
     * @return the fully constructed request URL
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
     * Generates an offline-mode UUID for the given player name.
     * <p>
     * Uses the standard Minecraft offline algorithm:
     * {@code UUID.nameUUIDFromBytes("OfflinePlayer:" + name)}.
     *
     * @param name the Minecraft player name
     * @return the generated offline UUID
     */
    public static UUID nameToOfflineUUID(String name)
    {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

    /**
     * Resolves a player name to an online (Mojang) UUID via HTTP API lookup.
     *
     * @param name the Minecraft player name
     * @return the online UUID, or {@code null} if no profile or valid UUID was found
     * @throws IOException if the HTTP request fails
     */
    public static UUID nameToOnlineUUID(String name) throws IOException
    {
        String response = HTTPHandler.get(buildNameToUuidUrl(name));

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No UUID found for online player '{}'.", name);
            return null;
        }
        JSONObject json = new JSONObject(response);
        String uuid = json.optString("id", "").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        if (uuid.isEmpty())
        {
            LOGGER.warn("Invalid UUID retrieved for name '{}'.", name);
            return null;
        }

        LOGGER.info("Retrieved online UUID for player '{}': {}", name, uuid);
        return UUID.fromString(uuid);
    }

    /**
     * Resolves an online UUID to a Minecraft player name via HTTP API lookup.
     *
     * @param uuid the online Minecraft player UUID
     * @return the player name, or {@code null} if no profile or name was found
     * @throws IOException if the HTTP request fails
     */
    public static String onlineUUIDToName(UUID uuid) throws IOException
    {
        String response = HTTPHandler.get(buildUuidToNameUrl(uuid));

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No profile found for UUID '{}'. This may be an offline/cracked UUID.", uuid);
            return null;
        }

        JSONObject json = new JSONObject(response);
        String name = json.optString("name", "");
        if (name.isEmpty())
        {
            LOGGER.warn("Response for UUID '{}' did not contain a name.", uuid);
            return null;
        }

        LOGGER.info("Successfully retrieved name: {} for UUID: {}", name, uuid);
        return name;
    }

    /**
     * Determines whether a UUID is an online, offline, or invalid Minecraft UUID by version.
     * <p>
     * Version {@code 4} is treated as online, version {@code 3} as offline; any other version is invalid.
     *
     * @param uuid the UUID to classify
     * @return the corresponding {@link UUIDType}
     */
    public static UUIDType getUUIDType(UUID uuid)
    {
        int v = uuid.version();
        if (v == 4) return ONLINE;
        if (v == 3) return OFFLINE;
        return INVALID;
    }

    /**
     * Checks whether the given string is a well-formed Minecraft online or offline UUID.
     * <p>
     * Requires the standard 36-character hyphenated form and a UUID version of {@code 3} or {@code 4}.
     *
     * @param uuidString the UUID string to validate
     * @return {@code true} if the string is a valid online or offline UUID; {@code false} otherwise
     */
    public static boolean isValidUUID(String uuidString)
    {
        if (uuidString == null || uuidString.length() != 36)
        {
            return false;
        }
        try
        {
            return getUUIDType(UUID.fromString(uuidString)) != INVALID;
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
