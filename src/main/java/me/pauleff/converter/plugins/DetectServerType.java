package me.pauleff.converter.plugins;

import me.pauleff.converter.api.DefaultPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static me.pauleff.converter.ServerType.*;

/**
 * Detects whether the server root is Vanilla, Bukkit-based, or modded and stores the
 * result on the {@link PluginContext}.
 * <p>
 * Defaults to Vanilla, then upgrades to Modded or Bukkit when characteristic folders
 * or files are present.
 */
public class DetectServerType implements DefaultPlugin
{
    private static final PluginMetadata META = PluginMetadata.of(
            "detect-server-type",
            "Detect Server Type",
            "Detect the Minecraft server type (Vanilla, Bukkit, Modded..).",
            0);

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginMetadata metadata()
    {
        return META;
    }

    /**
     * Returns the server root folder as the sole detection target.
     *
     * @param ctx the shared conversion context
     * @return a single-element list containing the server folder
     */
    @Override
    public List<Path> setTargets(PluginContext ctx)
    {
        return List.of(ctx.serverFolder());
    }

    /**
     * Sets the context server type to Vanilla, Modded, or Bukkit based on root layout.
     *
     * @param ctx                     the shared conversion context
     * @param resolvedExistingTargets unused; detection inspects the server folder from {@code ctx}
     * @throws IOException if I/O fails while detecting the server type
     */
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

    /**
     * Indicates whether the server root looks like a Bukkit/Spigot/Paper installation.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if Bukkit-style markers are present; {@code false} otherwise
     */
    private boolean isBukkit(PluginContext ctx)
    {
        Path serverRoot = ctx.serverFolder();
        return Files.isDirectory(serverRoot.resolve("plugins"))
                && Files.exists(serverRoot.resolve("commands.yml"))
                && (Files.exists(serverRoot.resolve("bukkit.yml"))
                || Files.exists(serverRoot.resolve("spigot.yml")));
    }

    /**
     * Indicates whether the server root looks like a Forge or Fabric installation.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if modded markers are present; {@code false} otherwise
     */
    private boolean isModded(PluginContext ctx)
    {
        Path serverRoot = ctx.serverFolder();
        return Files.isDirectory(serverRoot.resolve("mods"))
                || Files.isDirectory(serverRoot.resolve(".fabric"));
    }
}
