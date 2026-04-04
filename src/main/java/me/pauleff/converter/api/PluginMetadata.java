package me.pauleff.converter.api;

import me.pauleff.converter.PluginRegistry;

import java.util.Objects;

/**
 * Plugin identity for logs and {@link PluginRegistry}.
 * <p>
 * {@linkplain #priority() Priority}: lower values run earlier. Stored values are clamped to
 * {@link #MIN_PRIORITY}–{@link #MAX_PRIORITY}. Use {@link #of(String, String, String)} to omit priority
 * and get {@link #DEFAULT_PRIORITY}; use {@link #of(String, String, String, int)} when setting it
 * explicitly.
 *
 * @param id          Non-blank unique id for registration and management.
 * @param displayName Short plugin label for logging.
 * @param description This plugin's purpose; may be empty.
 * @param priority    Execution order (after clamping).
 */
public record PluginMetadata(String id, String displayName, String description, int priority)
{
    public static final int MIN_PRIORITY = 0;
    public static final int MAX_PRIORITY = 100;
    public static final int DEFAULT_PRIORITY = 50;

    /**
     * Metadata with {@link #DEFAULT_PRIORITY} (no explicit priority argument).
     */
    public static PluginMetadata of(String id, String displayName, String description)
    {
        return new PluginMetadata(id, displayName, description, DEFAULT_PRIORITY);
    }

    /**
     * Metadata with an explicit priority (clamped to {@link #MIN_PRIORITY}–{@link #MAX_PRIORITY}).
     */
    public static PluginMetadata of(String id, String displayName, String description, int priority)
    {
        return new PluginMetadata(id, displayName, description, priority);
    }

    /**
     * Clamps {@code p} to {@link #MIN_PRIORITY}–{@link #MAX_PRIORITY} inclusive.
     */
    private static int clampPriority(int p)
    {
        return Math.clamp(p, MIN_PRIORITY, MAX_PRIORITY);
    }

    /**
     * @throws IllegalArgumentException if {@code id} is blank
     */
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
}
