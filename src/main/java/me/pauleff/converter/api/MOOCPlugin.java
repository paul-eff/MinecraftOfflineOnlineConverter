package me.pauleff.converter.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public sealed interface MOOCPlugin permits DefaultPlugin, MultiServerPlugin, ServerTypePlugin
{
    default Logger logger()
    {
        return LoggerFactory.getLogger(getClass());
    }

    PluginMetadata metadata();

    List<Path> setTargets(PluginContext ctx);

    boolean isEnabled(PluginContext ctx);

    void run(PluginContext ctx, List<Path> resolvedExistingTargets) throws IOException;

    default List<Path> returnAllFilesInFolders(List<Path> worldDimensionRootFolders)
    {
        List<Path> targets = new ArrayList<>();
        for (Path rootFolder : worldDimensionRootFolders)
        {
            if (!Files.exists(rootFolder))
            {
                logger().debug("Skipping missing world folder: {}", rootFolder.normalize());
                continue;
            }
            try (Stream<Path> worldFolderStream = Files.walk(rootFolder))
            {
                worldFolderStream
                        .filter(path -> !path.equals(rootFolder))
                        .filter(Files::isRegularFile)
                        .forEach(targets::add);
            } catch (IOException e)
            {
                logger().warn("Could not collect world targets from {}", rootFolder.normalize(), e);
            }
        }
        return targets;
    }
}
