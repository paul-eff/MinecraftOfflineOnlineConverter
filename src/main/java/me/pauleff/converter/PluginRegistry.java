package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Holds the ordered plugin lists used for discovery, misc, and conversion phases.
 * <p>
 * Construction validates unique plugin ids and sorts each list by
 * {@link PluginMetadata#priority()} (lower first), preserving registration order
 * for equal priorities. {@link #standard()} supplies the built-in plugin set.
 *
 * @param discoveryPlugins  read-only / detection plugins that run before files are changed
 * @param miscPlugins       mutating plugins that can run for any server type after confirmation
 * @param conversionPlugins server-type-specific world conversion plugins
 * @see PluginOrchestrator
 * @see MOOCPlugin
 */
public record PluginRegistry(
        List<MOOCPlugin> discoveryPlugins,
        List<MOOCPlugin> miscPlugins,
        List<MOOCPlugin> conversionPlugins)
{
    private static final List<MOOCPlugin> DISCOVERY_PLUGINS = List.of(
            new DetectServerType(),
            new DetectWorldFolderStructure(),
            new DetectSaveFileFormat(),
            new PrefetchUsercache()
    );

    private static final List<MOOCPlugin> MISC_PLUGINS = List.of(
            new UpdateProperties(),
            new UpdateDefaultServerFiles(),
            new ApplyCliServerProperties(),
            new CopyCliPlayerData()
    );

    private static final List<MOOCPlugin> CONVERSION_PLUGINS = List.of(
            new ConvertVanillaServer(),
            new ConvertBukkitServer(),
            new ConvertModdedServer(),
            new ConvertFtbQuests()
    );

    /**
     * Validates non-null plugin lists, rejects duplicate plugin ids, and replaces each
     * list with an immutable copy sorted by priority then registration order.
     *
     * @throws NullPointerException     if any plugin list is {@code null}
     * @throws IllegalArgumentException if two plugins share the same {@link PluginMetadata#id()}
     */
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

    /**
     * Returns the built-in registry of discovery, misc, and conversion plugins.
     *
     * @return a {@link PluginRegistry} containing the standard plugin set
     */
    public static PluginRegistry standard()
    {
        return new PluginRegistry(DISCOVERY_PLUGINS, MISC_PLUGINS, CONVERSION_PLUGINS);
    }

    /**
     * Sorts plugins by ascending {@link PluginMetadata#priority()}, keeping equal-priority
     * plugins in their original list order.
     *
     * @param plugins the plugins to sort
     * @return a new list ordered by priority then index, or the same list when size is at most one
     */
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

    /**
     * Ensures every plugin has a unique {@link PluginMetadata#id()}.
     *
     * @param plugins the plugins to check across all phases
     * @throws NullPointerException     if any plugin's metadata is {@code null}
     * @throws IllegalArgumentException if a duplicate id is found
     */
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
