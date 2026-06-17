package me.pauleff.common.handlers;

import me.pauleff.converter.UUIDType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static me.pauleff.converter.UUIDType.*;

public class UUIDHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UUIDHandler.class);
    private static final HTTPHandler HTTP = new HTTPHandler();
    private static final String apiBasePath = "https://api.mojang.com/";

    public static UUID nameToOfflineUUID(String name)
    {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

    public static UUID nameToOnlineUUID(String name) throws IOException
    {
        HTTP.set(apiBasePath + "users/profiles/minecraft/" + name);
        String response = HTTP.get();

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
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString();

        HTTP.set(url);
        String response = HTTP.get();

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No profile found for UUID '{}'. This may be an offline/cracked UUID.", uuid);
            return null;
        }

        try
        {
            JSONObject json = new JSONObject(response);

            String name = json.optString("name", "");

            if (name.isEmpty())
            {
                LOGGER.warn("Response for UUID '{}' did not contain a name.", uuid);
                return null;
            }

            LOGGER.info("Successfully retrieved name: {} for UUID: {}", name, uuid);
            return name;

        } catch (Exception e)
        {
            LOGGER.error("Failed to parse Mojang API response for UUID: {}", uuid, e);
            return null;
        }
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
            UUID uuid = UUID.fromString(uuidString);
            return getUUIDType(uuid) != INVALID;
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
