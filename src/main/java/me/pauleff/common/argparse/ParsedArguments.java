package me.pauleff.common.argparse;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public record ParsedArguments(
        boolean verbose,
        Optional<Path> serverPath,
        Optional<Boolean> toOnlineMode,
        Optional<String> copyPlayerDataSourceWorld,
        Map<String, String> serverPropertiesChanges)
{
    public boolean hasPath()
    {
        return serverPath.isPresent();
    }

    public boolean isConversionOperation()
    {
        return toOnlineMode.isPresent();
    }

    public boolean shouldCopyPlayerData()
    {
        return copyPlayerDataSourceWorld.isPresent();
    }
}
