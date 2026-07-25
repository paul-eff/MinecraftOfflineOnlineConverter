package me.pauleff.converter.api;

import java.util.Objects;

/**
 * Holds identifying and scheduling information for a {@link MOOCPlugin}.
 * <p>
 * Priority is clamped to the inclusive range {@link #MIN_PRIORITY}–{@link #MAX_PRIORITY}
 * when the record is constructed. Factory methods supply a default priority when omitted.
 *
 * @param id          a non-blank unique plugin identifier
 * @param displayName a human-readable name shown in logs or UI
 * @param description a short description of what the plugin does
 * @param priority    the execution priority; lower values run earlier after clamping
 */
public record PluginMetadata(String id, String displayName, String description, int priority)
{
    public static final int MIN_PRIORITY = 0;
    public static final int MAX_PRIORITY = 100;
    public static final int DEFAULT_PRIORITY = 50;

    /**
     * Validates non-null record components, rejects a blank {@code id}, and clamps
     * {@code priority} into the allowed range.
     *
     * @throws NullPointerException     if {@code id}, {@code displayName}, or {@code description} is {@code null}
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

    /**
     * Creates metadata with {@link #DEFAULT_PRIORITY}.
     *
     * @param id          a non-blank unique plugin identifier
     * @param displayName a human-readable name shown in logs or UI
     * @param description a short description of what the plugin does
     * @return a new {@link PluginMetadata} instance
     */
    public static PluginMetadata of(String id, String displayName, String description)
    {
        return new PluginMetadata(id, displayName, description, DEFAULT_PRIORITY);
    }

    /**
     * Creates metadata with an explicit priority (clamped on construction).
     *
     * @param id          a non-blank unique plugin identifier
     * @param displayName a human-readable name shown in logs or UI
     * @param description a short description of what the plugin does
     * @param priority    the desired execution priority before clamping
     * @return a new {@link PluginMetadata} instance
     */
    public static PluginMetadata of(String id, String displayName, String description, int priority)
    {
        return new PluginMetadata(id, displayName, description, priority);
    }

    /**
     * Clamps a priority value to the inclusive {@link #MIN_PRIORITY}–{@link #MAX_PRIORITY} range.
     *
     * @param p the priority to clamp
     * @return {@code p} constrained to the allowed range
     */
    private static int clampPriority(int p)
    {
        return Math.clamp(p, MIN_PRIORITY, MAX_PRIORITY);
    }
}
