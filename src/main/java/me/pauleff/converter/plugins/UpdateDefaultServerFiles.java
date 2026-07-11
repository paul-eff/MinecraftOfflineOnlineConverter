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
            "usercache.json");

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
        return SERVER_FILE_NAMES.stream()
                .map(name -> ctx.serverFolder().resolve(name))
                .toList();
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        for (Path path : resolvedExistingTargets)
        {
            updateUuidReferences(ctx, path);
        }
    }

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
        logger().info("Updated file: {}", ctx.serverFolder().relativize(path));
    }
}
