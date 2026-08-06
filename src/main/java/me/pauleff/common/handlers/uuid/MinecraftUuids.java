package me.pauleff.common.handlers.uuid;

import me.pauleff.converter.UUIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.UUID.nameUUIDFromBytes;

/**
 * Provides pure helpers for Minecraft UUID generation, formatting, parsing, and classification.
 */
public final class MinecraftUuids
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftUuids.class);
    private static final Pattern UNDASHED_UUID = Pattern.compile(
            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private MinecraftUuids()
    {
    }

    /**
     * Generates an offline-mode UUID for the given player name.
     * <p>
     * Uses the standard Minecraft offline algorithm:
     * {@code UUID.nameUUIDFromBytes("OfflinePlayer:" + name)}.
     *
     * @param name the Minecraft player name
     * @return the generated offline UUID
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static UUID offlineFromName(String name)
    {
        UUID uuid = nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        LOGGER.debug("Offline UUID generated for '{}': {}", name, uuid);
        return uuid;
    }

    /**
     * Returns the hyphenated string form of a UUID without dashes.
     * <p>
     * Mojang APIs and some mod formats (for example FTB Quests) store UUIDs this way.
     *
     * @param uuid the UUID to format
     * @return the 32-character dashless UUID string
     * @throws NullPointerException if {@code uuid} is {@code null}
     */
    public static String dashless(UUID uuid)
    {
        return Objects.requireNonNull(uuid, "uuid").toString().replace("-", "");
    }

    /**
     * Parses a hyphenated or dashless UUID string.
     * <p>
     * Accepts the standard 36-character form and the 32-character Mojang API form.
     *
     * @param uuidString the UUID string to parse
     * @return the parsed UUID
     * @throws NullPointerException     if {@code uuidString} is {@code null}
     * @throws IllegalArgumentException if {@code uuidString} is not a valid UUID string
     */
    public static UUID parse(String uuidString)
    {
        Objects.requireNonNull(uuidString, "uuidString");
        if (uuidString.length() == 32)
        {
            return UUID.fromString(UNDASHED_UUID.matcher(uuidString).replaceAll("$1-$2-$3-$4-$5"));
        }
        return UUID.fromString(uuidString);
    }

    /**
     * Determines whether a UUID is an online, offline, or invalid Minecraft UUID by version.
     * <p>
     * Version {@code 4} is treated as online, version {@code 3} as offline; any other version is invalid.
     *
     * @param uuid the UUID to classify
     * @return the corresponding {@link UUIDType}
     * @throws NullPointerException if {@code uuid} is {@code null}
     */
    public static UUIDType typeOf(UUID uuid)
    {
        return switch (uuid.version())
        {
            case 4 -> UUIDType.ONLINE;
            case 3 -> UUIDType.OFFLINE;
            default -> UUIDType.INVALID;
        };
    }

    /**
     * Checks whether the given string is a well-formed Minecraft online or offline UUID.
     * <p>
     * Requires the standard 36-character hyphenated form and a UUID version of {@code 3} or {@code 4}.
     *
     * @param uuidString the UUID string to validate
     * @return {@code true} if the string is a valid online or offline UUID; {@code false} otherwise
     */
    public static boolean isValid(String uuidString)
    {
        if (uuidString == null || uuidString.length() != 36)
        {
            return false;
        }
        try
        {
            return typeOf(UUID.fromString(uuidString)) != UUIDType.INVALID;
        } catch (IllegalArgumentException _)
        {
            return false;
        }
    }
}
