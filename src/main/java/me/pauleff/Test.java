package me.pauleff;

import me.pauleff.handlers.UUIDHandler;
import org.json.JSONObject;
import java.io.IOException;
import java.util.UUID;
import java.io.IOException;
import java.util.UUID;

public class Test {




    /**
     * To get a name from a UUID, you must use the Session Server:
     * https://sessionserver.mojang.com/session/minecraft/profile/<UUID>
     *
     * To get a UUID from a name, you use the API Server:
     * https://api.mojang.com/users/profiles/minecraft/<Username>
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        UUID offlineUUID = UUID.fromString("420b1de8-75bb-3b70-8743-63ce0f6816c9");
        String s = UUIDHandler.onlineUUIDToName(offlineUUID);
        System.out.println(s);

        UUID onlineUUID = UUID.fromString(addDashes("4bf81e8e3b56493d9f1779263532ac20"));
        String s2 = UUIDHandler.onlineUUIDToName(onlineUUID);
        System.out.println(s2);
    }

    public static String addDashes(String trimmedUUID) {
        if (trimmedUUID == null || trimmedUUID.length() != 32) {
            throw new IllegalArgumentException("Invalid trimmed UUID length: " + trimmedUUID);
        }

        return new StringBuilder(trimmedUUID)
                .insert(20, "-")
                .insert(16, "-")
                .insert(12, "-")
                .insert(8, "-")
                .toString();
    }
}
