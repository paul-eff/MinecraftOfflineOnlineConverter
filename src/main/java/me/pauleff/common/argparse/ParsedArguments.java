package me.pauleff.common.argparse;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the successfully parsed CLI options that drive conversion and related operations.
 *
 * @param serverPath                the path to the server folder, if specified
 * @param toOnlineMode              {@code true} for online conversion, {@code false} for offline,
 *                                  or empty when no conversion was requested
 * @param copyPlayerDataSourceWorld the source world folder name to copy player data from, if requested
 * @param serverPropertiesChanges   key/value pairs to apply to {@code server.properties}; may be empty
 */
public record ParsedArguments(
        Optional<Path> serverPath,
        Optional<Boolean> toOnlineMode,
        Optional<String> copyPlayerDataSourceWorld,
        Map<String, String> serverPropertiesChanges)
{
    /**
     * Indicates whether an online/offline conversion was requested.
     *
     * @return {@code true} if {@code toOnlineMode} is present; {@code false} otherwise
     */
    public boolean isConversionOperation()
    {
        return toOnlineMode.isPresent();
    }

    /**
     * Indicates whether player data should be copied from another world.
     *
     * @return {@code true} if {@code copyPlayerDataSourceWorld} is present; {@code false} otherwise
     */
    public boolean shouldCopyPlayerData()
    {
        return copyPlayerDataSourceWorld.isPresent();
    }
}
