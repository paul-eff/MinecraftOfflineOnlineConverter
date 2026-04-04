package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.converter.api.PluginMetadata;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * For each {@link MOOCPlugin}: resolve {@linkplain MOOCPlugin#setTargets(PluginContext) targets},
 * do some checks and then apply conversion.
 */
public final class PluginOrchestrator
{

    private final PluginRegistry registry;

    public PluginOrchestrator(PluginRegistry registry)
    {
        this.registry = Objects.requireNonNull(registry, "Registry can't be null.");
    }

    public void run(PluginContext ctx)
    {
        Objects.requireNonNull(ctx, "Context can't be null.");
        Path serverRoot = ctx.serverFolder();
        for (MOOCPlugin plugin : registry.plugins())
        {
            PluginMetadata meta = Objects.requireNonNull(plugin.metadata(), "Plugin metadata can't be null.");
            String pluginId = meta.id();
            Logger pluginLog = plugin.logger();
            try
            {
                if (!plugin.isEnabled(ctx))
                {
                    pluginLog.debug("Skipped: disabled for this context.");
                    continue;
                }
                List<Path> declared = plugin.setTargets(ctx);
                Objects.requireNonNull(declared, () -> "targetPaths() returned null for plugin " + pluginId);

                List<Path> resolvedExisting = new ArrayList<>();
                List<Path> missing = new ArrayList<>();
                for (Path relative : declared)
                {
                    Path absolute = serverRoot.resolve(relative).normalize();
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
                    continue;
                }
                pluginLog.info("Running plugin: {} ({})", meta.displayName(), pluginId);
                if (!meta.description().isBlank())
                {
                    pluginLog.debug("{}", meta.description());
                }
                pluginLog.debug("Targets: {}", resolvedExisting);
                plugin.run(ctx, List.copyOf(resolvedExisting));
            } catch (IOException | RuntimeException e)
            {
                pluginLog.error("Plugin failed: {}", e.getMessage(), e);
            }
        }
    }
}
