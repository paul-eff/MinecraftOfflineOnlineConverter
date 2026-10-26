package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.files.UsercacheFile;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.UUIDType;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static me.pauleff.common.handlers.uuid.MinecraftUuids.offlineFromName;
import static me.pauleff.common.handlers.uuid.MinecraftUuids.typeOf;
import static me.pauleff.common.handlers.uuid.OnlineProfileLookup.nameToOnlineUuid;

/**
 * Prefills the context UUID map from {@code usercache.json} for an online/offline conversion.
 * <p>
 * Online conversion requires cached player names. Offline cache entries are resolved via
 * the configured profile API; online cache entries supply the online UUID directly.
 */
public class PrefetchUsercache implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "prefetch-usercache",
            "Prefetch usercache",
            "Reads usercache.json and fills UUID mappings for online/offline conversion.",
            2);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns {@code true} when an online/offline conversion was requested.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if this is a conversion operation; {@code false} otherwise
     */
    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation();
    }

    /**
     * Returns {@code usercache.json} under the server folder when available.
     * A full-server run exits with the legacy-version notice when the file is missing.
     * Custom targets continue without prefetched profiles.
     *
     * @param ctx the shared conversion context
     * @return the usercache path, or an empty list if it is missing for a custom target
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        Path usercachePath = ctx.serverFolder().resolve("usercache.json");

        if (!Files.isRegularFile(usercachePath))
        {
        /*
         * During a normal conversion run this is the second point where we can determine if a conversion is even needed.
         * The easiest way to determine this is if the usercache.json file was generated.
         * If not, this almost always hints to the server being pre Minecraft 1.7.6 (2014).
         */
            if (!ctx.isScopedConversion())
            {
                logger().info("""
                        You are probably trying to convert a Minecraft version older than 1.7.6 (2014). Switching between online and offline mode should work without conversion.
                        If this is a mistake, please report it to the MOOC repository.""");
                System.exit(0);
            }

            logger().warn("No usercache.json found at {}. Online conversion requires a usercache containing the players' names and UUIDs.",
                    usercachePath);
            return List.of();
        }

        return List.of(usercachePath);
    }

    /**
     * Loads known players from each resolved usercache and stores UUID remappings on the context.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing usercache paths to read
     * @throws IOException if reading a usercache file fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        for (Path path : resolvedExistingTargets)
        {
            prefetchFromUsercache(path, ctx);
        }
    }

    /**
     * Reads players from a usercache file and maps each UUID toward the conversion target mode.
     *
     * @param path the usercache.json path
     * @param ctx  the shared conversion context
     */
    private void prefetchFromUsercache(Path path, PluginContext ctx)
    {
        JSONArray knownPlayers = UsercacheFile.loadArray(path);
        int prefetched = 0;

        for (Object obj : knownPlayers)
        {
            if (!(obj instanceof JSONObject knownPlayer))
            {
                continue;
            }

            try
            {
                String playerName = knownPlayer.getString("name");
                UUID playerUUID = UUID.fromString(knownPlayer.getString("uuid"));
                UUIDType playerType = typeOf(playerUUID);
                if (playerName.isBlank() || playerType == UUIDType.INVALID)
                {
                    logger().warn("Skipping usercache entry with a blank name or unsupported UUID: {}", knownPlayer);
                    continue;
                }

                if (ctx.conversionTarget() == ConversionTarget.ONLINE)
                {
                    UUID onlineUUID = playerType == UUIDType.ONLINE ? playerUUID : nameToOnlineUuid(playerName);
                    if (onlineUUID == null || typeOf(onlineUUID) != UUIDType.ONLINE)
                    {
                        logger().warn("Skipping '{}' — no valid online UUID found from the configured profile API.", playerName);
                        continue;
                    }
                    // Offline UUIDs depend on the exact spelling of the cached name. A cache
                    // copied from online mode still provides enough information for this mapping.
                    UUID offlineUUID = playerType == UUIDType.OFFLINE ? playerUUID : offlineFromName(playerName);
                    ctx.putUuidMapping(offlineUUID, onlineUUID);
                    logger().info("Prefetched {} -> {}", playerName, onlineUUID);
                    prefetched++;
                } else
                {
                    UUID offlineUUID = offlineFromName(playerName);
                    ctx.putUuidMapping(playerUUID, offlineUUID);
                    logger().info("Prefetched {} -> {}", playerName, offlineUUID);
                    prefetched++;
                }
            } catch (IOException e)
            {
                logger().warn("Could not resolve usercache entry {} using the configured profile API.", knownPlayer, e);
            } catch (JSONException | IllegalArgumentException e)
            {
                logger().warn("Skipping invalid usercache entry or profile response for {}: {}", knownPlayer, e.getMessage());
            }
        }

        logger().info("Prefetched {} player profile(s) from usercache.", prefetched);
    }
}
