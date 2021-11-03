package me.paulferlitz;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDHandler
{
    private static HTTPHandler http = new HTTPHandler();

    public static UUID offlineNameToUUID(String offlineName)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
    }

    public static UUID onlineNameToUUID(String onlineName) throws IOException
    {
        http.setUrl("https://api.mojang.com/users/profiles/minecraft/" + onlineName);
        JSONObject json = new JSONObject(http.httpDoGet());
        String uuid = json.getString("id").replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(uuid);
    }

    public static String onlineUUIDToName(UUID onlineUUID) throws IOException
    {
        http.setUrl("https://api.mojang.com/user/profile/" + onlineUUID.toString());
        JSONObject json = new JSONObject(http.httpDoGet());
        return json.getString("name");
    }

    public static UUID onlineUUIDToOffline(UUID onlineUUID) throws IOException
    {
        return offlineNameToUUID(onlineUUIDToName(onlineUUID));
    }
}
