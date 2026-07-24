package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record PluginRegistry(
        List<MOOCPlugin> discoveryPlugins,
        List<MOOCPlugin> miscPlugins,
        List<MOOCPlugin> conversionPlugins)
{
    /**
     * Read-only / detection plugins that run for any server type before files are changed.
     */
    private static final List<MOOCPlugin> DISCOVERY_PLUGINS = List.of(
            new DetectServerType(),
            new DetectWorldFolderStructure(),
            new DetectSaveFileFormat(),
            new PrefetchUsercache()
    );

    /**
     * Mutating plugins that can and should run for any server type, after confirmation.
     */
    private static final List<MOOCPlugin> MISC_PLUGINS = List.of(
            new UpdateProperties(),
            new UpdateDefaultServerFiles(),
            new ApplyCliServerProperties(),
            new CopyCliPlayerData()
    );

    /**
     * Add all your plugins to this which can run on one or multiple server types.
     */
    private static final List<MOOCPlugin> CONVERSION_PLUGINS = List.of(
            new ConvertVanillaServer(),
            new ConvertBukkitServer(),
            new ConvertModdedServer()
    );

    public PluginRegistry
    {
        Objects.requireNonNull(discoveryPlugins, "Discovery plugins list can't be null.");
        Objects.requireNonNull(miscPlugins, "Misc plugins list can't be null.");
        Objects.requireNonNull(conversionPlugins, "Conversion plugins list can't be null.");
        assertUniquePluginIds(Stream.of(discoveryPlugins, miscPlugins, conversionPlugins)
                .flatMap(Collection::stream)
                .toList());
        discoveryPlugins = List.copyOf(sortByPriorityThenIndex(discoveryPlugins));
        miscPlugins = List.copyOf(sortByPriorityThenIndex(miscPlugins));
        conversionPlugins = List.copyOf(sortByPriorityThenIndex(conversionPlugins));
    }

    public static PluginRegistry standard()
    {
        return new PluginRegistry(DISCOVERY_PLUGINS, MISC_PLUGINS, CONVERSION_PLUGINS);
    }

    private static List<MOOCPlugin> sortByPriorityThenIndex(List<MOOCPlugin> plugins)
    {
        if (plugins.size() <= 1)
        {
            return plugins;
        }
        return IntStream.range(0, plugins.size())
                .boxed()
                .sorted(Comparator
                        .comparing((Integer index) -> plugins.get(index).metadata().priority())
                        .thenComparingInt(index -> index))
                .map(plugins::get)
                .toList();
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
