package me.pauleff.converter;

import me.pauleff.common.exceptions.UnknownWorldFolderStructureException;
import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.UpdateDefaultServerFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        ctx.setUsercacheEmptyBeforeConversion(ctx.uuidMap().isEmpty());

        if (!ctx.isConversionOperation())
        {
            return;
        }

        if (ctx.conversionTarget() == ConversionTarget.ONLINE && ctx.uuidMap().isEmpty())
        {
            LOGGER.error("No profiles resolved for online conversion. Aborting...");
            return;
        }

        ServerType serverType = Objects.requireNonNull(ctx.serverType(), "Server type can't be null");

        boolean hasMatchingConversionPlugin = registry.conversionPlugins().stream()
                .anyMatch(plugin -> plugin.isEnabled(ctx));
        if (!hasMatchingConversionPlugin)
        {
            LOGGER.warn("No plugin list found for server type {}", serverType.name());
        }

        runPhase(ctx, registry.conversionPlugins());
        /*
         * A good measure run as there can be cases where usercache.json is not being populated by Minecraft.
         * In this case we hope to discover some UUIDs during conversion to finalize the default files.
         */
        if (ctx.usercacheEmptyBeforeConversion())
        {
            runPlugin(ctx, new UpdateDefaultServerFiles());
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
