package me.pauleff.converter.plugins;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static me.pauleff.converter.ServerType.*;

public class DetectServerType implements MOOCPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-server-type",
            "Detect Server Type",
            "Detect the Minecraft server type (Vanilla, Bukkit, Modded..).",
            0);

    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder());
    }

    @Override
    public void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException
    {
        ctx.setServerType(VANILLA);
        if (isModded(ctx))
        {
            ctx.setServerType(MODDED);
        } else if (isBukkit(ctx))
        {
            ctx.setServerType(BUKKIT);
        }
        logger().info("Detected server type: {}", ctx.serverType().name());
    }

    private boolean isBukkit(PluginContext ctx)
    {
        Path serverRoot = ctx.serverFolder();
        return Files.isDirectory(serverRoot.resolve("plugins"))
                && Files.exists(serverRoot.resolve("commands.yml"))
                && (Files.exists(serverRoot.resolve("bukkit.yml"))
                || Files.exists(serverRoot.resolve("spigot.yml")));
    }

    private boolean isModded(PluginContext ctx)
    {
        Path serverRoot = ctx.serverFolder();
        return Files.isDirectory(serverRoot.resolve("mods"))
                || Files.isDirectory(serverRoot.resolve(".fabric"));
    }
}
