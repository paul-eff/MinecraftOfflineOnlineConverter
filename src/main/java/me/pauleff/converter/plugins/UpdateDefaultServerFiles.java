package me.pauleff.converter.plugins;

import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rewrites remapped player UUIDs in root server JSON files such as whitelist, bans, and ops.
 */
public class UpdateDefaultServerFiles implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "update-default-server-files",
            "Update default server files",
            "Rewrites UUIDs in root server files (whitelist, bans, ops, etc.).",
            3);

    private static final List<String> SERVER_FILE_NAMES = List.of(
            "whitelist.json",
            "banned-players.json",
            "banned-ips.json",
            "ops.json",
            "usercache.json",
            "usernamecache.json");

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
     * Returns the standard server JSON files under the server root that may contain UUIDs.
     *
     * @param ctx the shared conversion context
     * @return the candidate server file paths; never {@code null}
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return SERVER_FILE_NAMES.stream()
                .map(name -> ctx.serverFolder().resolve(name))
                .toList();
    }

    /**
     * Replaces original UUIDs with remapped values in each resolved server file.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets the existing server files to rewrite
     * @throws IOException if reading or writing a file fails
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        int updated = 0;
        for (Path path : resolvedExistingTargets)
        {
            updateUuidReferences(ctx, path);
            updated++;
        }
        if (updated > 0)
        {
            logger().info("Updated {} default server file(s).", updated);
        }
    }

    /**
     * Replaces every mapped UUID string in the given file with its remapped counterpart.
     *
     * @param ctx  the shared conversion context holding the UUID map
     * @param path the server file to update
     * @throws IOException if reading or writing the file fails
     */
    private void updateUuidReferences(PluginContext ctx, Path path) throws IOException
    {
        String fileContent = Files.readString(path);

        for (Map.Entry<UUID, UUID> entry : ctx.uuidMap().entrySet())
        {
            UUID targetUuid = ctx.getTargetUuid(entry.getKey());
            if (targetUuid == null)
            {
                continue;
            }
            fileContent = fileContent.replace(entry.getKey().toString(), targetUuid.toString());
        }

        Files.writeString(path, fileContent);
        logger().debug("Updated file: {}", ctx.serverFolder().relativize(path));
    }
}
