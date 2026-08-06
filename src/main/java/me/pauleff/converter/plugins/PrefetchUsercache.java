package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.files.UsercacheFile;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static me.pauleff.common.handlers.uuid.MinecraftUuids.offlineFromName;
import static me.pauleff.common.handlers.uuid.OnlineProfileLookup.nameToOnlineUuid;

/**
 * Prefills the context UUID map from {@code usercache.json} for an online/offline conversion.
 * <p>
 * Exits early when {@code usercache.json} is missing, which usually indicates a pre-1.7.6 server
 * that does not need UUID conversion.
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
     * Returns {@code usercache.json} under the server folder, or exits if the file is absent.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the usercache path
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        Path usercachePath = ctx.serverFolder().resolve("usercache.json");

        /*
         * During a normal conversion run this is the second point where we can determine if a conversion is even needed.
         * The easiest way to determine this is if the usercache.json file was generated.
         * If not, this almost always hints to the server being pre Minecraft 1.7.6 (2014).
         */
        if (!Files.exists(usercachePath))
        {
            logger().info("""
                    You are probably trying to convert a Minecraft older then Minecraft 1.7.6 (2014). Switching between online and offline should work without any conversion needed!
                    If this is a mistake, please report this case to the MOOC repository.""");
            System.exit(0);
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

                if (ctx.conversionTarget() == ConversionTarget.ONLINE)
                {
                    UUID onlineUUID = nameToOnlineUuid(playerName);
                    if (onlineUUID == null)
                    {
                        logger().warn("Skipping '{}' — no online UUID found (Mojang API).", playerName);
                        continue;
                    }
                    ctx.putUuidMapping(playerUUID, onlineUUID);
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
                logger().warn("There was an error whilst fetching information from the Mojang API.", e);
            }
        }

        logger().info("Prefetched {} player profile(s) from usercache.", prefetched);
    }
}
