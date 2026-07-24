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

public final class PluginOrchestrator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginOrchestrator.class);

    private final PluginRegistry registry;

    public PluginOrchestrator()
    {
        this(PluginRegistry.standard());
    }

    public PluginOrchestrator(PluginRegistry registry)
    {
        this.registry = Objects.requireNonNull(registry, "Registry can't be null.");
    }

    private static Path resolvePath(Path serverRoot, Path declaredPath)
    {
        return declaredPath.isAbsolute()
                ? declaredPath.normalize()
                : serverRoot.resolve(declaredPath).normalize();
    }

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

    private boolean confirmConversion(PluginContext ctx)
    {
        ConversionTarget target = ctx.conversionTarget();
        ConversionTarget source = target == ConversionTarget.ONLINE
                ? ConversionTarget.OFFLINE
                : ConversionTarget.ONLINE;

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

    private void runPhase(PluginContext ctx, List<MOOCPlugin> plugins)
    {
        for (MOOCPlugin plugin : plugins)
        {
            runPlugin(ctx, plugin);
        }
    }

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
                pluginLog.info("No existing targets; skipping operate.");
                return;
            }
            pluginLog.info("Running plugin: {} ({})", meta.displayName(), pluginId);
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
