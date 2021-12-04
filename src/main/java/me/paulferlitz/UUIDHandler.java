package me.paulferlitz;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Class to handle UUID related actions.
 */
public class UUIDHandler
{
    // Class variables
    private static HTTPHandler http = new HTTPHandler();

    /**
     * Method for converting a player name to an offline UUID.
     *
     * @param offlineName Player's name.
     * @return The resulting UUID.
     */
    public static UUID offlineNameToUUID(String offlineName)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Method for converting a player name to an online UUID.
     *
     * @param onlineName Player's name.
     * @return The resulting UUID.
     * @throws IOException If there were connection issues.
     */
    public static UUID onlineNameToUUID(String onlineName) throws IOException
    {
        // Set target URL to Mojang API
        http.setUrl("https://api.mojang.com/users/profiles/minecraft/" + onlineName);
        // Parse UUID from response
        JSONObject json = new JSONObject(http.httpDoGet());
        String uuid = json.getString("id").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(uuid);
    }

    /**
     * Method to get a player's name from an online UUID.
     *
     * @param onlineUUID The player's UUID.
     * @return The resulting name.
     * @throws IOException If there were connection issues.
     */
    public static String onlineUUIDToName(UUID onlineUUID) throws IOException
    {
        // Set target URL to Mojang API
        http.setUrl("https://api.mojang.com/user/profile/" + onlineUUID.toString());
        // Parse name from response
        JSONObject json = new JSONObject(http.httpDoGet());
        return json.getString("name");
    }

    /**
     * Method that bundles the other methods to one online to offline method call.
     * @param onlineUUID The player's online UUID.
     * @return The player's offline UUID.
     * @throws IOException If there were connection issues.
     */
    public static UUID onlineUUIDToOffline(UUID onlineUUID) throws IOException
    {
        return offlineNameToUUID(onlineUUIDToName(onlineUUID));
    }
}
