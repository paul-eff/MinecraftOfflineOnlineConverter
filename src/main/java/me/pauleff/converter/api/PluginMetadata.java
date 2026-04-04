package me.pauleff.converter.api;

import me.pauleff.converter.PluginRegistry;

import java.util.Objects;

/**
 * Plugin identity for logs and {@link PluginRegistry}.
 *
 * @param id          Non-blank unique id plugin registration and management.
 * @param displayName Short plugin label for logging.
 * @param description This plugin's purpose; may be empty.
 */
public record PluginMetadata(String id, String displayName, String description)
{
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
    }
}
