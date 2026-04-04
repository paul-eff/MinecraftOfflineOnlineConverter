package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static me.pauleff.common.handlers.FileHandler.loadArrayFromUsercache;
import static me.pauleff.common.handlers.UUIDHandler.nameToOfflineUUID;

public class PrefetchUsercache implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "prefetch-usercache",
            "Prefetch usercache",
            "Reads usercache.json and fills UUID mappings for online/offline conversion.",
            0);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Paths relative to e.g. {@link PluginContext#serverFolder()} and which will be
     * the target of this plugin's conversion.
     *
     * @param ctx {@link PluginContext} holding information on folders and
     *            conversion target.
     * @return {@link List} of {@link Path}s to convert; non-null; may be empty
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder().resolve("usercache.json"));
    }

    /**
     * Do work only on {@code resolvedExistingTargets} (absolute, existing).
     *
     * @param ctx                     {@link PluginContext} holding information on
     *                                folders and conversion target.
     * @param resolvedExistingTargets {@link List} of {@link Path}s to convert or
     *                                further work on.
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        resolvedExistingTargets.forEach(path -> {
            JSONArray knownPlayers = loadArrayFromUsercache(path);

            for (Object obj : knownPlayers)
            {
                if (!(obj instanceof JSONObject knownPlayer)) continue;

                try
                {
                    String playerName = knownPlayer.getString("name");
                    UUID playerUUID = UUID.fromString(knownPlayer.getString("uuid"));

                    if (ctx.toOnlineMode())
                    {
                        UUID onlineUUID = UUIDHandler.nameToOnlineUUID(playerName);

                        if (onlineUUID == null)
                        {
                            logger().warn("Skipping '{}' — no online UUID found (Mojang API).", playerName);
                            continue;
                        }
                        ctx.putUuidMapping(playerUUID, onlineUUID);
                        logger().info("Prefetched {} -> {}", playerName, onlineUUID);
                    } else
                    {
                        UUID offlineUUID = nameToOfflineUUID(playerName);
                        ctx.putUuidMapping(playerUUID, offlineUUID);
                        logger().info("Prefetched {} -> {}", playerName, offlineUUID);
                    }
                } catch (IOException e)
                {
                    logger().warn("There was an error whilst fetching information from the Mojang API.", e);
                }
            }
        });
    }
}
