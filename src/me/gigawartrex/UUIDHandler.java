package me.gigawartrex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDHandler
{
    HTTPHandler http;

    public UUIDHandler()
    {
        this.http = new HTTPHandler();
    }

    public UUID offlineNameToUUID(String offlineName)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + offlineName).getBytes(StandardCharsets.UTF_8));
    }

    public UUID onlineNameToUUID(String onlineName) throws IOException
    {
        http.setUrl("https://api.mojang.com/users/profiles/minecraft/" + onlineName);
        String content = http.httpDoGet();
        String rawUUID = content.substring(content.indexOf("\"id\":") + 6, content.length() - 2);
        String uuid = rawUUID.replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(uuid);
    }

    public String onlineUUIDToName(String onlineUUID) throws IOException
    {
        http.setUrl("https://api.mojang.com/user/profile/" + onlineUUID);
        String content = http.httpDoGet();
        return content.substring(content.indexOf("\"name\":") + 8, content.length() - 2);
    }

    public String onlineUUIDToOffline(String onlineUUID) throws IOException
    {
        return offlineNameToUUID(onlineUUIDToName(onlineUUID)).toString();
    }
}
