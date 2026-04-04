package me.pauleff.converter;

import me.pauleff.converter.api.MOOCPlugin;
import me.pauleff.converter.api.PluginMetadata;
import me.pauleff.converter.plugins.*;
import me.pauleff.detection.MinecraftFlavor;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Immutable ordered {@link MOOCPlugin} lists. Register plugins in the list that matches your case.
 */
public record PluginRegistry(List<MOOCPlugin> plugins)
{
    /**
     * Plugins that run for every {@link MinecraftFlavor}.
     */
    private static final List<MOOCPlugin> DEFAULT_PLUGINS = List.of(
            new PrefetchUsercache(),
            new UpdateProperties(),
            new UpdateDefaultServerFiles()
    );

    /**
     * Plugins for {@link MinecraftFlavor#VANILLA} only.
     */
    private static final List<MOOCPlugin> VANILLA_PLUGINS = List.of(
            new VanillaWorld()
    );

    /**
     * Plugins for {@link MinecraftFlavor#LIGHT_MODDED} only (e.g. Paper, Spigot).
     */
    private static final List<MOOCPlugin> LIGHT_MODDED_PLUGINS = List.of(
            new LightModdedWorld()
    );

    /**
     * Plugins for {@link MinecraftFlavor#MODDED} only (e.g. Forge, Fabric).
     */
    private static final List<MOOCPlugin> MODDED_PLUGINS = List.of(
            new ModdedWorld()
    );

    /**
     * @throws NullPointerException     if {@code plugins}, any entry, or {@code metadata()} is null
     * @throws IllegalArgumentException on duplicate {@link PluginMetadata#id()}
     */
    public PluginRegistry(List<MOOCPlugin> plugins)
    {
        Objects.requireNonNull(plugins, "Plugins List can't be null.");
        assertUniquePluginIds(plugins);
        this.plugins = List.copyOf(plugins);
    }

    private static void assertUniquePluginIds(List<MOOCPlugin> plugins)
    {
        Set<String> seen = new HashSet<>();
        for (MOOCPlugin plugin : plugins)
        {
            Objects.requireNonNull(plugin, "Passed plugin can't be null.");
            PluginMetadata meta = Objects.requireNonNull(plugin.metadata(), "Plugin metadata can't be null.");
            String id = meta.id();
            if (!seen.add(id))
            {
                throw new IllegalArgumentException(
                        String.format("Duplicate plugin id \"%s\". Each %s must use a unique %s#id().",
                                id,
                                MOOCPlugin.class.getSimpleName(),
                                PluginMetadata.class.getSimpleName())
                );
            }
        }
    }

    /**
     * Unmodifiable; plugins for all {@link MinecraftFlavor}s.
     */
    public static List<MOOCPlugin> defaultPlugins()
    {
        return DEFAULT_PLUGINS;
    }

    /**
     * Unmodifiable; {@link MinecraftFlavor#VANILLA}-specific plugins.
     */
    public static List<MOOCPlugin> vanillaPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), VANILLA_PLUGINS.stream()).toList();
    }

    /**
     * Unmodifiable; {@link MinecraftFlavor#LIGHT_MODDED}-specific plugins.
     */
    public static List<MOOCPlugin> lightModdedPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), LIGHT_MODDED_PLUGINS.stream()).toList();
    }

    /**
     * Unmodifiable; {@link MinecraftFlavor#MODDED}-specific plugins.
     */
    public static List<MOOCPlugin> moddedPlugins()
    {
        return Stream.concat(DEFAULT_PLUGINS.stream(), MODDED_PLUGINS.stream()).toList();
    }
}
