package me.pauleff.common.handlers.uuid;

import java.util.UUID;

/**
 * Immutable endpoint configuration for online Minecraft profile lookups.
 * <p>
 * Endpoint-specific URLs override the shared custom base URL, which in turn overrides
 * the Mojang / Minecraft Services defaults.
 */
public record ProfileApiConfig(
        String customApiBaseUrl,
        String retrieveUuidUrl,
        String retrieveNameUrl
)
{
    private static final String DEFAULT_NAME_API_BASE = "https://api.mojang.com/";
    private static final String DEFAULT_UUID_API_BASE = "https://api.minecraftservices.com/";

    /**
     * Returns a config that uses Mojang defaults for all lookups.
     *
     * @return the default profile API configuration
     */
    public static ProfileApiConfig defaults()
    {
        return new ProfileApiConfig(null, null, null);
    }

    /**
     * Creates a config with normalized URL fields.
     *
     * @param customApiBaseUrl shared custom API base URL, or {@code null}/blank to unset
     * @param retrieveUuidUrl  name-to-UUID endpoint prefix, or {@code null}/blank to unset
     * @param retrieveNameUrl  UUID-to-name endpoint prefix, or {@code null}/blank to unset
     */
    public ProfileApiConfig
    {
        customApiBaseUrl = normalizeApiUrl(customApiBaseUrl);
        retrieveUuidUrl = normalizeApiUrl(retrieveUuidUrl);
        retrieveNameUrl = normalizeApiUrl(retrieveNameUrl);
    }

    /**
     * Returns a copy with a different shared custom API base URL.
     *
     * @param url the custom API base URL, or {@code null}/blank to clear it
     * @return a new config with the updated base URL
     */
    public ProfileApiConfig withCustomApiBaseUrl(String url)
    {
        return new ProfileApiConfig(url, retrieveUuidUrl, retrieveNameUrl);
    }

    /**
     * Returns a copy with a different name-to-UUID endpoint prefix.
     * <p>
     * When set, this overrides {@link #customApiBaseUrl()} and the default Mojang endpoint.
     * The player name is appended to the given URL.
     *
     * @param url the custom retrieve-UUID URL prefix, or {@code null}/blank to clear it
     * @return a new config with the updated retrieve-UUID URL
     */
    public ProfileApiConfig withRetrieveUuidUrl(String url)
    {
        return new ProfileApiConfig(customApiBaseUrl, url, retrieveNameUrl);
    }

    /**
     * Returns a copy with a different UUID-to-name endpoint prefix.
     * <p>
     * When set, this overrides {@link #customApiBaseUrl()} and the default Minecraft Services
     * endpoint. The UUID string is appended to the given URL.
     *
     * @param url the custom retrieve-name URL prefix, or {@code null}/blank to clear it
     * @return a new config with the updated retrieve-name URL
     */
    public ProfileApiConfig withRetrieveNameUrl(String url)
    {
        return new ProfileApiConfig(customApiBaseUrl, retrieveUuidUrl, url);
    }

    /**
     * Builds the request URL used to resolve a player name to an online UUID.
     *
     * @param name the Minecraft player name
     * @return the fully constructed request URL
     */
    public String nameToUuidUrl(String name)
    {
        if (retrieveUuidUrl != null)
        {
            return retrieveUuidUrl + name;
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
    public String uuidToNameUrl(UUID uuid)
    {
        if (retrieveNameUrl != null)
        {
            return retrieveNameUrl + uuid;
        }
        String base = customApiBaseUrl != null ? customApiBaseUrl : DEFAULT_UUID_API_BASE;
        return base + "minecraft/profile/lookup/" + uuid;
    }

    private static String normalizeApiUrl(String url)
    {
        if (url == null || url.isBlank())
        {
            return null;
        }
        return url.endsWith("/") ? url : url + "/";
    }
}
