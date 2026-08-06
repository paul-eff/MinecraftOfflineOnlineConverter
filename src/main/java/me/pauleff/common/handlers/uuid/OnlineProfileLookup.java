package me.pauleff.common.handlers.uuid;

import me.pauleff.common.handlers.http.HttpGet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Resolves online Minecraft player profiles via HTTP API lookups.
 * <p>
 * Prefer the static one-shot methods for single operations against the process-wide
 * configuration. Use {@link #of(ProfileApiConfig)} when supplying a custom config,
 * or {@link #configure(ProfileApiConfig)} once at startup (for example from CLI flags).
 */
public final class OnlineProfileLookup
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineProfileLookup.class);

    private static OnlineProfileLookup current = of(ProfileApiConfig.defaults());

    private final ProfileApiConfig config;
    private final HttpGet http;

    private OnlineProfileLookup(ProfileApiConfig config, HttpGet http)
    {
        this.config = config;
        this.http = http;
    }

    /**
     * Returns a lookup that uses the given API configuration and the default HTTP client.
     *
     * @param config the profile API endpoint configuration
     * @return an online profile lookup
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public static OnlineProfileLookup of(ProfileApiConfig config)
    {
        return of(config, HttpGet.ofDefault());
    }

    /**
     * Returns a lookup that uses the given API configuration and HTTP client.
     *
     * @param config the profile API endpoint configuration
     * @param http   the HTTP GET client
     * @return an online profile lookup
     * @throws NullPointerException if {@code config} or {@code http} is {@code null}
     */
    public static OnlineProfileLookup of(ProfileApiConfig config, HttpGet http)
    {
        return new OnlineProfileLookup(
                Objects.requireNonNull(config, "config"),
                Objects.requireNonNull(http, "http"));
    }

    /**
     * Replaces the process-wide lookup used by the static one-shot methods.
     * <p>
     * Called once at startup when custom API URLs are provided on the command line.
     *
     * @param config the profile API endpoint configuration
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public static void configure(ProfileApiConfig config)
    {
        current = of(config);
        logConfiguredEndpoints(config);
    }

    /**
     * Returns the process-wide lookup installed via {@link #configure(ProfileApiConfig)},
     * or the Mojang-default lookup when nothing custom was configured.
     *
     * @return the current online profile lookup
     */
    public static OnlineProfileLookup current()
    {
        return current;
    }

    /**
     * Resolves a player name to an online UUID using the process-wide lookup.
     *
     * @param name the Minecraft player name
     * @return the online UUID, or {@code null} if no profile or valid UUID was found
     * @throws IOException if the HTTP request fails
     */
    public static UUID nameToOnlineUuid(String name) throws IOException
    {
        return current().lookupOnlineUuid(name);
    }

    /**
     * Resolves an online UUID to a player name using the process-wide lookup.
     *
     * @param uuid the online Minecraft player UUID
     * @return the player name, or {@code null} if no profile or name was found
     * @throws IOException if the HTTP request fails
     */
    public static String onlineUuidToName(UUID uuid) throws IOException
    {
        return current().lookupName(uuid);
    }

    /**
     * Returns the API configuration used by this lookup.
     *
     * @return the profile API configuration
     */
    public ProfileApiConfig config()
    {
        return config;
    }

    /**
     * Resolves a player name to an online (Mojang) UUID via HTTP API lookup.
     *
     * @param name the Minecraft player name
     * @return the online UUID, or {@code null} if no profile or valid UUID was found
     * @throws IOException if the HTTP request fails
     */
    public UUID lookupOnlineUuid(String name) throws IOException
    {
        String response = http.getBody(config.nameToUuidUrl(name));

        if (response == null || response.isEmpty())
        {
            LOGGER.warn("No UUID found for online player '{}'.", name);
            return null;
        }

        JSONObject json = new JSONObject(response);
        String rawId = json.optString("id", "");
        if (rawId.isEmpty())
        {
            LOGGER.warn("Invalid UUID retrieved for name '{}'.", name);
            return null;
        }

        try
        {
            UUID uuid = MinecraftUuids.parse(rawId);
            LOGGER.debug("Retrieved online UUID for player '{}': {}", name, uuid);
            return uuid;
        } catch (IllegalArgumentException e)
        {
            LOGGER.warn("Invalid UUID retrieved for name '{}'.", name);
            return null;
        }
    }

    /**
     * Resolves an online UUID to a Minecraft player name via HTTP API lookup.
     *
     * @param uuid the online Minecraft player UUID
     * @return the player name, or {@code null} if no profile or name was found
     * @throws IOException if the HTTP request fails
     */
    public String lookupName(UUID uuid) throws IOException
    {
        String response = http.getBody(config.uuidToNameUrl(uuid));

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

        LOGGER.debug("Successfully retrieved name: {} for UUID: {}", name, uuid);
        return name;
    }

    private static void logConfiguredEndpoints(ProfileApiConfig config)
    {
        if (config.customApiBaseUrl() != null)
        {
            LOGGER.info("Using custom base URL for online UUID/player name resolution: {}",
                    config.customApiBaseUrl());
        }
        if (config.retrieveUuidUrl() != null)
        {
            LOGGER.info("Using custom URL for retrieving online UUIDs: {}", config.retrieveUuidUrl());
        }
        if (config.retrieveNameUrl() != null)
        {
            LOGGER.info("Using custom URL for retrieving online player names: {}", config.retrieveNameUrl());
        }
    }
}
