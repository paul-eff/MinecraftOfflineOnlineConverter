package me.pauleff.converter.api;

import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.exceptions.PathNotValidException;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.SaveFileFormat;
import me.pauleff.converter.ServerType;
import me.pauleff.converter.WorldFolderStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public final class PluginContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginContext.class);

    private final Path serverFolder;
    private final Path worldFolder;
    private final ConversionTarget conversionTarget;
    private final Map<UUID, UUID> uuidMap;
    private ServerType serverType;
    private WorldFolderStructure worldFolderStructure;
    private SaveFileFormat saveFileFormat;
    private final ParsedArguments parsedArguments;

    private PluginContext(
            Path serverFolder,
            Path worldFolder,
            ConversionTarget conversionTarget,
            ParsedArguments parsedArguments)
    {
        this.serverFolder = Objects.requireNonNull(serverFolder, "Server folder path can't be null.");
        this.worldFolder = Objects.requireNonNull(worldFolder, "World folder path can't be null.");
        this.conversionTarget = Objects.requireNonNull(conversionTarget, "Target to convert to must be set.");
        this.parsedArguments = Objects.requireNonNull(parsedArguments, "Parsed arguments can't be null.");
        this.uuidMap = new HashMap<>();
    }

    public static PluginContext from(ParsedArguments parsedArgs) throws PathNotValidException
    {
        Objects.requireNonNull(parsedArgs, "Parsed arguments can't be null.");

        Path serverFolder = parsedArgs.serverPath()
                .orElse(Path.of("."))
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(serverFolder))
        {
            throw new PathNotValidException("Server folder not found", serverFolder);
        }
        LOGGER.info("Server folder set to: {}", serverFolder);

        Path serverProperties = serverFolder.resolve("server.properties");
        if (!Files.exists(serverProperties))
        {
            throw new PathNotValidException(
                    "Could not find server.properties",
                    serverProperties.toAbsolutePath().normalize());
        }

        String worldName = FileHandler.readWorldNameFromProperties(serverProperties);
        Path worldFolder = serverFolder.resolve(worldName);
        if (!Files.exists(worldFolder))
        {
            throw new PathNotValidException(worldFolder.toAbsolutePath().normalize());
        }

        ConversionTarget conversionTarget = parsedArgs.toOnlineMode()
                .map(online -> online ? ConversionTarget.ONLINE : ConversionTarget.OFFLINE)
                .orElse(ConversionTarget.OFFLINE);

        return new PluginContext(serverFolder, worldFolder, conversionTarget, parsedArgs);
    }

    public void putUuidMapping(UUID from, UUID to)
    {
        uuidMap.put(
                Objects.requireNonNull(from, "Original UUID to put into map can't be null."),
                Objects.requireNonNull(to, "New UUID to put into map can't be null."));
    }

    public UUID getTargetUuid(UUID from)
    {
        return uuidMap.get(Objects.requireNonNull(from, "Original UUID to put into map can't be null."));
    }

    public Path serverFolder()
    {
        return serverFolder;
    }

    public Path worldFolder()
    {
        return worldFolder;
    }

    public ConversionTarget conversionTarget()
    {
        return conversionTarget;
    }

    public ServerType serverType()
    {
        return serverType;
    }

    public WorldFolderStructure worldFolderStructure()
    {
        return worldFolderStructure;
    }

    public SaveFileFormat worldSaveFileFormat()
    {
        return saveFileFormat;
    }

    public Map<UUID, UUID> uuidMap()
    {
        return uuidMap;
    }

    public void setServerType(ServerType serverType)
    {
        this.serverType = serverType;
    }

    public void setWorldFolderStructure(WorldFolderStructure worldFolderStructure)
    {
        this.worldFolderStructure = worldFolderStructure;
    }

    public void setSaveFileFormat(SaveFileFormat saveFileFormat)
    {
        this.saveFileFormat = saveFileFormat;
    }

    public ParsedArguments parsedArguments()
    {
        return parsedArguments;
    }

    public boolean isConversionOperation()
    {
        return parsedArguments.isConversionOperation();
    }

    @Override
    public String toString()
    {
        return "PluginContext{" +
                "serverFolder=" + serverFolder +
                ", worldFolder=" + worldFolder +
                ", conversionTarget=" + conversionTarget.name() +
                ", serverType=" + serverType.name() +
                ", worldFolderStructure=" + worldFolderStructure.name() +
                ", saveFileFormat=" + saveFileFormat.name() +
                ", uuidMap=" + uuidMap +
                '}';
    }
}
