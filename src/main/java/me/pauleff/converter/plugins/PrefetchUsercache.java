package me.pauleff.converter.plugins;

import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static me.pauleff.common.handlers.FileHandler.loadArrayFromUsercache;

public class PrefetchUsercache implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "prefetch-usercache",
            "Prefetch usercache",
            "Reads usercache.json and fills UUID mappings for online/offline conversion.",
            2);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public boolean isEnabled(PluginContext ctx)
    {
        return ctx.isConversionOperation();
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder().resolve("usercache.json"));
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        for (Path path : resolvedExistingTargets)
        {
            prefetchFromUsercache(path, ctx);
        }
    }

    private void prefetchFromUsercache(Path path, PluginContext ctx)
    {
        JSONArray knownPlayers = loadArrayFromUsercache(path);

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
                    UUID offlineUUID = UUIDHandler.nameToOfflineUUID(playerName);
                    ctx.putUuidMapping(playerUUID, offlineUUID);
                    logger().info("Prefetched {} -> {}", playerName, offlineUUID);
                }
            } catch (IOException e)
            {
                logger().warn("There was an error whilst fetching information from the Mojang API.", e);
            }
        }
    }
}
