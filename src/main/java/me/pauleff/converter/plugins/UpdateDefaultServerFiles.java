package me.pauleff.converter.plugins;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

public class UpdateDefaultServerFiles implements MOOCPlugin
{
    private static final PluginMetadata META = new PluginMetadata(
            "update-default-server-files",
            "Update default server files",
            "Rewrites UUIDs in root server files (whitelist, bans, ops, etc.).");

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
        List<String> fileNames = List.of(
                "whitelist.json",
                "banned-players.json",
                "banned-ips.json",
                "ops.json",
                "usercache.json",
                "whitelist.json");

        return fileNames.stream()
                .map(name -> ctx.serverFolder().resolve(name))
                .toList();
    }

    /**
     * Do work only on {@code resolvedExistingTargets} (absolute, existing).
     *
     * @param ctx                     {@link PluginContext} holding information on folders and conversion target.
     * @param resolvedExistingTargets {@link List} of {@link Path}s to convert or further work on.
     */
    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        resolvedExistingTargets.forEach(path -> {
            try
            {
                String fileContent = readString(path);

                for (var entry : ctx.uuidMap().entrySet())
                {
                    UUID origUUID = entry.getKey();
                    fileContent = fileContent.replace(origUUID.toString(), ctx.getTargetUuid(origUUID).toString());
                }

                writeString(path, fileContent);
            } catch (IOException e)
            {
                throw new RuntimeException("Failed to update file: " + path, e);
            }
        });
    }
}
