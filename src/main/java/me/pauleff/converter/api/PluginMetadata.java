package me.pauleff.converter.api;

import me.pauleff.converter.PluginRegistry;

import java.util.Objects;

public record PluginMetadata(String id, String displayName, String description, int priority)
{
    public static final int MIN_PRIORITY = 0;
    public static final int MAX_PRIORITY = 100;
    public static final int DEFAULT_PRIORITY = 50;

        public PluginMetadata
    {
        Objects.requireNonNull(id, "ID can't be null.");
        Objects.requireNonNull(displayName, "DisplayName can't be null.");
        Objects.requireNonNull(description, "Description can't be null.");
        if (id.isBlank())
        {
            throw new IllegalArgumentException("ID must not be blank.");
        }
        priority = clampPriority(priority);
    }

        public static PluginMetadata of(String id, String displayName, String description)
    {
        return new PluginMetadata(id, displayName, description, DEFAULT_PRIORITY);
    }

        public static PluginMetadata of(String id, String displayName, String description, int priority)
    {
        return new PluginMetadata(id, displayName, description, priority);
    }

        private static int clampPriority(int p)
    {
        return Math.clamp(p, MIN_PRIORITY, MAX_PRIORITY);
    }
}
