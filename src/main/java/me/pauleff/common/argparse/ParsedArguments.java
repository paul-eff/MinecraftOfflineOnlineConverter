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
 * @param conversionFolder          a folder to limit conversion to, if specified
 * @param conversionFile            a single file to limit conversion to, if specified
 * @param serverPropertiesChanges   key/value pairs to apply to {@code server.properties}; may be empty
 */
public record ParsedArguments(
        Optional<Path> serverPath,
        Optional<Boolean> toOnlineMode,
        Optional<String> copyPlayerDataSourceWorld,
        Optional<Path> conversionFolder,
        Optional<Path> conversionFile,
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

    /**
     * Indicates whether conversion should be limited to a single folder.
     *
     * @return {@code true} if {@code conversionFolder} is present; {@code false} otherwise
     */
    public boolean isFolderScopedConversion()
    {
        return conversionFolder.isPresent();
    }

    /**
     * Indicates whether conversion should be limited to a single file.
     *
     * @return {@code true} if {@code conversionFile} is present; {@code false} otherwise
     */
    public boolean isFileScopedConversion()
    {
        return conversionFile.isPresent();
    }

    /**
     * Indicates whether conversion is limited to a custom folder or file.
     *
     * @return {@code true} if {@code conversionFolder} or {@code conversionFile} is present
     */
    public boolean isScopedConversion()
    {
        return isFolderScopedConversion() || isFileScopedConversion();
    }
}
