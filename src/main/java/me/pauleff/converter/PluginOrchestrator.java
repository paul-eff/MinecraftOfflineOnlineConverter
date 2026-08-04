package me.pauleff.converter;

import me.pauleff.common.exceptions.UnknownWorldFolderStructureException;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.UpdateDefaultServerFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runs registered {@link MOOCPlugin}s against a {@link PluginContext} in phased order.
 * <p>
 * Discovery plugins always run first. For conversion operations, the user must confirm
 * before misc and conversion plugins execute. Paths declared by each plugin are resolved
 * relative to the server root; only existing targets are passed to {@link MOOCPlugin#run}.
 *
 * @see PluginRegistry
 * @see PluginContext
 * @see MOOCPlugin
 */
public final class PluginOrchestrator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginOrchestrator.class);

    private final PluginRegistry registry;

    /**
     * Creates an orchestrator that uses {@link PluginRegistry#standard()}.
     */
    public PluginOrchestrator()
    {
        this(PluginRegistry.standard());
    }

    /**
     * Creates an orchestrator that uses the given plugin registry.
     *
     * @param registry the registry supplying discovery, misc, and conversion plugins
     * @throws NullPointerException if {@code registry} is {@code null}
     */
    public PluginOrchestrator(PluginRegistry registry)
    {
        this.registry = Objects.requireNonNull(registry, "Registry can't be null.");
    }

    /**
     * Resolves a plugin-declared path against the server root when it is relative.
     *
     * @param serverRoot   the absolute server root folder
     * @param declaredPath the path declared by a plugin
     * @return the normalized absolute path
     */
    private static Path resolvePath(Path serverRoot, Path declaredPath)
    {
        return declaredPath.isAbsolute()
                ? declaredPath.normalize()
                : serverRoot.resolve(declaredPath).normalize();
    }

    /**
     * Executes the full plugin pipeline for the given context.
     * <p>
     * Always runs discovery plugins. For conversion operations, aborts when an online
     * conversion has an empty UUID map or when the user declines confirmation. Misc plugins
     * then run; conversion plugins run only when a conversion was requested. If conversion
     * discovers additional UUID mappings, {@link UpdateDefaultServerFiles} is reapplied.
     *
     * @param ctx the shared conversion context
     * @throws NullPointerException                 if {@code ctx} is {@code null}, or if the
     *                                              server type is unset when conversion plugins run
     * @throws UnknownWorldFolderStructureException if a plugin reports an unsupported world layout
     */
    public void run(PluginContext ctx)
    {
        Objects.requireNonNull(ctx, "Context can't be null.");
        runPhase(ctx, registry.discoveryPlugins());

        if (ctx.isConversionOperation())
        {
            if (ctx.conversionTarget() == ConversionTarget.ONLINE && ctx.uuidMap().isEmpty())
            {
                LOGGER.error("No profiles resolved for online conversion. Aborting...");
                return;
            }
            if (!confirmConversion(ctx))
            {
                LOGGER.info("Conversion cancelled.");
                return;
            }
            LOGGER.info("Conversion confirmed. Proceeding...");
        }

        runPhase(ctx, registry.miscPlugins());

        if (!ctx.isConversionOperation())
        {
            return;
        }

        ServerType serverType = Objects.requireNonNull(ctx.serverType(), "Server type can't be null");

        boolean hasMatchingConversionPlugin = registry.conversionPlugins().stream()
                .anyMatch(plugin -> plugin.isEnabled(ctx));
        if (!hasMatchingConversionPlugin)
        {
            LOGGER.warn("No plugin list found for server type {}", serverType.name());
        }

        int uuidMapSizeBeforeConversion = ctx.uuidMap().size();
        runPhase(ctx, registry.conversionPlugins());
        /*
         * If conversion discovered additional UUID mappings (e.g. empty usercache.json),
         * re-run default server file updates with the newly found mappings.
         */
        if (ctx.uuidMap().size() > uuidMapSizeBeforeConversion)
        {
            LOGGER.info("The number of detected profiles has increased during the conversion run. Reapplying to the server's default files.");
            runPlugin(ctx, new UpdateDefaultServerFiles());
        }
    }

    /**
     * Prompts the user on standard input to confirm an in-place conversion.
     * <p>
     * Accepts {@code y} or {@code yes} (case-insensitive). Any other answer, a failed
     * read, or EOF declines conversion.
     *
     * @param ctx the shared conversion context
     * @return {@code true} if the user confirmed; {@code false} otherwise
     */
    private boolean confirmConversion(PluginContext ctx)
    {
        LOGGER.warn("""
                WARNING! Read before proceeding:
                
                Please confirm everything above was detected correctly.
                Make a backup of your server files before continuing.
                Conversion will modify files in place and cannot be undone without a backup.
                """);

        try
        {
            System.out.print("Start conversion? [y/N]: ");
            System.out.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            String answer = reader.readLine();
            return answer != null && (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
        } catch (IOException e)
        {
            LOGGER.error("Failed to read confirmation input: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Runs each plugin in the given list against the context.
     *
     * @param ctx     the shared conversion context
     * @param plugins the plugins to run in order
     */
    private void runPhase(PluginContext ctx, List<MOOCPlugin> plugins)
    {
        for (MOOCPlugin plugin : plugins)
        {
            runPlugin(ctx, plugin);
        }
    }

    /**
     * Runs a single plugin when enabled and at least one declared target exists on disk.
     * <p>
     * Plugin lifecycle details (skip reasons, missing paths, start, targets) are logged at debug.
     * {@link IOException} and {@link RuntimeException} from the plugin are logged without aborting
     * the pipeline; {@link UnknownWorldFolderStructureException} is rethrown.
     *
     * @param ctx    the shared conversion context
     * @param plugin the plugin to evaluate and possibly run
     * @throws UnknownWorldFolderStructureException if the plugin reports an unsupported world layout
     * @throws NullPointerException                 if the plugin's metadata is {@code null}, or if
     *                                              {@link MOOCPlugin#setTargets} returns {@code null}
     */
    private void runPlugin(PluginContext ctx, MOOCPlugin plugin)
    {
        Path serverRoot = ctx.serverFolder();
        PluginMetadata meta = Objects.requireNonNull(plugin.metadata(), "Plugin metadata can't be null.");
        String pluginId = meta.id();
        Logger pluginLog = plugin.logger();
        try
        {
            if (!plugin.isEnabled(ctx))
            {
                pluginLog.debug("Skipped: disabled for this context.");
                return;
            }
            List<Path> declared = plugin.setTargets(ctx);
            Objects.requireNonNull(declared, () -> "setTargets() returned null for plugin " + pluginId);

            List<Path> resolvedExisting = new ArrayList<>();
            List<Path> missing = new ArrayList<>();
            for (Path declaredPath : declared)
            {
                Path absolute = resolvePath(serverRoot, declaredPath);
                if (Files.exists(absolute))
                {
                    resolvedExisting.add(absolute);
                } else
                {
                    missing.add(absolute);
                }
            }
            if (!missing.isEmpty())
            {
                pluginLog.debug("Declared paths not on disk: {}", missing);
            }
            if (resolvedExisting.isEmpty())
            {
                pluginLog.warn("No existing targets; skipping operate.");
                return;
            }
            pluginLog.debug("Running plugin: {} ({})", meta.displayName(), pluginId);
            if (!meta.description().isBlank())
            {
                pluginLog.debug("{}", meta.description());
            }
            pluginLog.debug("Targets: {}", resolvedExisting);
            plugin.run(ctx, List.copyOf(resolvedExisting));
        } catch (UnknownWorldFolderStructureException e)
        {
            throw e;
        } catch (IOException | RuntimeException e)
        {
            pluginLog.error("Plugin failed: {}", e.getMessage(), e);
        }
    }
}
