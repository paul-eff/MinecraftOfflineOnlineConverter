package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable {@link MOOCPlugin} list. Plugins are ordered by {@link PluginMetadata#priority()} (lower
 * runs first), then by registration order in the input list when priorities tie.
 */
public record PluginRegistry(List<MOOCPlugin> plugins)
{
    /**
     * Plugins that run for every {@link ServerType}.
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
     * Plugins for {@link ServerType#VANILLA} only.
     */
    private static final List<MOOCPlugin> VANILLA_PLUGINS = List.of(
            new ConvertVanillaServer()
    );

    /**
     * Plugins for {@link ServerType#BUKKIT} only (e.g. Paper, Spigot).
     */
    private static final List<MOOCPlugin> BUKKIT_PLUGINS = List.of(
            new ConvertPluginServer()
    );

    /**
     * Plugins for {@link ServerType#MODDED} only (e.g. Forge, Fabric).
     */
    private static final List<MOOCPlugin> MODDED_PLUGINS = List.of(
            new ConvertModdedServer()
    );

    /**
     * @throws NullPointerException     if {@code plugins}, any entry, or {@code metadata()} is null
     * @throws IllegalArgumentException on duplicate {@link PluginMetadata#id()}
     */
    public PluginRegistry(List<MOOCPlugin> plugins)
    {
        Objects.requireNonNull(plugins, "Plugins List can't be null.");
        assertUniquePluginIds(plugins);
        this.plugins = List.copyOf(sortByPriorityThenIndex(plugins));
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

    /**
     * Unmodifiable; plugins for all {@link ServerType}s.
     */
    public static List<MOOCPlugin> defaultPlugins()
    {
        return DEFAULT_PLUGINS;
    }

    /**
     * Unmodifiable; {@link ServerType#VANILLA}-specific plugins.
     */
    public static List<MOOCPlugin> vanillaPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), VANILLA_PLUGINS.stream()).toList();
    }

    /**
     * Unmodifiable; {@link ServerType#BUKKIT}-specific plugins.
     */
    public static List<MOOCPlugin> bukkitPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), BUKKIT_PLUGINS.stream()).toList();
    }

    /**
     * Unmodifiable; {@link ServerType#MODDED}-specific plugins.
     */
    public static List<MOOCPlugin> moddedPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), MODDED_PLUGINS.stream()).toList();
    }
}
