package me.pauleff.common.argparse;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public record ParsedArguments(
        boolean verbose,
        boolean devMode,
        Optional<Path> serverPath,
        Optional<Boolean> toOnlineMode,
        boolean movePlayerData,
        Optional<String> movePlayerdataSourceDir,
        Map<String, String> serverPropertiesChanges)
{
    public boolean hasPath()
    {
        return serverPath.isPresent();
    }

    public boolean shouldConvert()
    {
        return toOnlineMode.isPresent();
    }
}
