package me.pauleff.common.handlers;

import me.pauleff.converter.UUIDType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static me.pauleff.converter.UUIDType.*;

public final class UUIDHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHandler.class);
    private static final String DEFAULT_NAME_API_BASE = "https://api.mojang.com/";
    private static final String DEFAULT_UUID_API_BASE = "https://api.minecraftservices.com/";
    private static String customApiBaseUrl = null;
    private static String retrieveUUIDUrl = null;
    private static String retrieveNameUrl = null;

    public static void setCustomApiBaseUrl(String url)
    {
        customApiBaseUrl = normalizeApiUrl(url);
        if (customApiBaseUrl != null)
        {
            LOGGER.info("Using custom base URL for online UUID/player name resolution: {}", customApiBaseUrl);
        }
    }

    public static void setRetrieveUUIDUrl(String url)
    {
        retrieveUUIDUrl = normalizeApiUrl(url);
        if (retrieveUUIDUrl != null)
        {
            LOGGER.info("Using custom URL for retrieving online UUIDs: {}", retrieveUUIDUrl);
        }
    }

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

    private static String buildNameToUuidUrl(String name)
    {
        if (retrieveUUIDUrl != null)
        {
            return retrieveUUIDUrl + name;
        }
        String base = customApiBaseUrl != null ? customApiBaseUrl : DEFAULT_NAME_API_BASE;
        return base + "users/profiles/minecraft/" + name;
    }

    private static String buildUuidToNameUrl(UUID uuid)
    {
        if (retrieveNameUrl != null)
        {
            return retrieveNameUrl + uuid.toString();
        }
        String base = customApiBaseUrl != null ? customApiBaseUrl : DEFAULT_UUID_API_BASE;
        return base + "minecraft/profile/lookup/" + uuid.toString();
    }

    public static UUID nameToOfflineUUID(String name)
    {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

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

    public static UUIDType getUUIDType(UUID uuid)
    {
        int v = uuid.version();
        if (v == 4) return ONLINE;
        if (v == 3) return OFFLINE;
        return INVALID;
    }

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
