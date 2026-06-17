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
    private static final PluginMetadata META = PluginMetadata.of(
            "update-default-server-files",
            "Update default server files",
            "Rewrites UUIDs in root server files (whitelist, bans, ops, etc.).",
            3);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

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
                Path serverFolder = ctx.serverFolder();
                Path serverNamePath = serverFolder.getFileName();
                String serverName = (serverNamePath != null ? serverNamePath : serverFolder)
                        .toString()
                        .replace('\\', '/');
                Path relativeToServer = serverFolder.relativize(path);
                String relativePart = relativeToServer.toString().replace('\\', '/');
                if (relativePart.isEmpty())
                {
                    logger().info("Updated file: .../{}", serverName);
                } else
                {
                    logger().info("Updated file: .../{}/{}", serverName, relativePart);
                }
            } catch (IOException e)
            {
                throw new RuntimeException("Failed to update file: " + path, e);
            }
        });
    }
}
