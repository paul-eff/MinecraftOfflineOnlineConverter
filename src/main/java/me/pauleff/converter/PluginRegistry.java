package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.api.MultiServerPlugin;
import me.pauleff.converter.plugins.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable plugin lists for the two-phase pipeline: discovery plugins populate
 * {@link me.pauleff.converter.api.PluginContext}, then conversion plugins run for the detected
 * {@link ServerType}. Within each list, plugins are ordered by {@link PluginMetadata#priority()}
 * (lower runs first), then by registration order when priorities tie.
 */
public record PluginRegistry(List<MOOCPlugin> discoveryPlugins, List<MOOCPlugin> conversionPlugins)
{
    /**
     * !!! Only add plugins to this list if it can and should run for ANY server type !!!
     */
    private static final List<MOOCPlugin> DEFAULT_PLUGINS = List.of(
            new DetectServerType(),
            new DetectWorldFolderStructure(),
            new DetectSaveFileFormat(),
            new PrefetchUsercache(),
            new UpdateProperties(),
            new UpdateDefaultServerFiles()
    );

    /**
     * Add all your plugins (implementing the {@link MultiServerPlugin} interface) to this
     */
    private static final List<MOOCPlugin> CONVERSION_PLUGINS = List.of(
            new ConvertVanillaServer(),
            new ConvertBukkitServer(),
            new ConvertModdedServer()
    );

    public PluginRegistry
    {
        Objects.requireNonNull(discoveryPlugins, "Discovery plugins list can't be null.");
        Objects.requireNonNull(conversionPlugins, "Conversion plugins list can't be null.");
        assertUniquePluginIds(Stream.concat(discoveryPlugins.stream(), conversionPlugins.stream()).toList());
        discoveryPlugins = List.copyOf(sortByPriorityThenIndex(discoveryPlugins));
        conversionPlugins = List.copyOf(sortByPriorityThenIndex(conversionPlugins));
    }

    public static PluginRegistry standard()
    {
        return new PluginRegistry(DEFAULT_PLUGINS, CONVERSION_PLUGINS);
    }

    /**
     * Stable sort: ascending {@link PluginMetadata#priority()}, then original list index.
     */
    private static List<MOOCPlugin> sortByPriorityThenIndex(List<MOOCPlugin> plugins)
    {
        int n = plugins.size();
        if (n <= 1)
        {
            return plugins;
        }
        MOOCPlugin[] arr = plugins.toArray(MOOCPlugin[]::new);
        Integer[] ord = IntStream.range(0, n).boxed().toArray(Integer[]::new);
        Arrays.sort(ord, Comparator
                .comparingInt((Integer i) -> arr[i].metadata().priority())
                .thenComparingInt(i -> i));
        return Arrays.stream(ord).map(i -> arr[i]).toList();
    }

    private static void assertUniquePluginIds(List<MOOCPlugin> plugins)
    {
        Set<String> seen = new HashSet<>();
        for (MOOCPlugin plugin : plugins)
        {
            PluginMetadata meta = Objects.requireNonNull(plugin.metadata(), "Plugin metadata can't be null.");
            String id = meta.id();
            if (!seen.add(id))
            {
                throw new IllegalArgumentException("Duplicate plugin id \"%s\". Each %s must use a unique %s#id().".formatted(
                        id,
                        MOOCPlugin.class.getSimpleName(),
                        PluginMetadata.class.getSimpleName())
                );
            }
        }
    }
}
