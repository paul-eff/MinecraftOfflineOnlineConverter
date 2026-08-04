package me.pauleff.converter.api;

import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.exceptions.PathNotValidException;
import me.pauleff.common.handlers.files.ServerPropertiesFile;
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

/**
 * Holds shared state for a conversion run and is passed to each {@link MOOCPlugin}.
 * <p>
 * Created from {@link ParsedArguments}, it resolves the server and world folders and
 * accumulates detection results (server type, folder structure, save format) plus
 * UUID remappings produced during conversion.
 */
public final class PluginContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginContext.class);

    private final Path serverFolder;
    private final Path worldFolder;
    private final ConversionTarget conversionTarget;
    private final Map<UUID, UUID> uuidMap;
    private final ParsedArguments parsedArguments;
    private ServerType serverType;
    private WorldFolderStructure worldFolderStructure;
    private SaveFileFormat saveFileFormat;

    /**
     * Creates a context with the given folders, conversion target, and parsed arguments.
     * <p>
     * Detection fields start unset and the UUID map is empty.
     *
     * @param serverFolder     the absolute, normalized server root folder
     * @param worldFolder      the world folder resolved from {@code server.properties}
     * @param conversionTarget whether to convert toward online or offline mode
     * @param parsedArguments  the CLI arguments for this run
     */
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

    /**
     * Builds a {@link PluginContext} from successfully parsed CLI arguments.
     * <p>
     * Resolves the server folder (defaulting to the current directory), requires
     * {@code server.properties}, derives the world folder from {@code level-name},
     * and sets the conversion target from the online/offline flags (offline when absent).
     *
     * @param parsedArgs the parsed CLI arguments
     * @return a new context ready for plugin execution
     * @throws PathNotValidException if the server folder, {@code server.properties}, or world folder is missing
     * @throws NullPointerException  if {@code parsedArgs} is {@code null}
     */
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

        String worldName = ServerPropertiesFile.worldName(serverProperties);
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

    /**
     * Records a UUID remapping from an original player UUID to its converted counterpart.
     *
     * @param from the original UUID
     * @param to   the remapped UUID
     * @throws NullPointerException if {@code from} or {@code to} is {@code null}
     */
    public void putUuidMapping(UUID from, UUID to)
    {
        uuidMap.put(
                Objects.requireNonNull(from, "Original UUID to put into map can't be null."),
                Objects.requireNonNull(to, "New UUID to put into map can't be null."));
    }

    /**
     * Returns the remapped UUID previously stored for the given original UUID.
     *
     * @param from the original UUID to look up
     * @return the remapped UUID, or {@code null} if no mapping exists
     * @throws NullPointerException if {@code from} is {@code null}
     */
    public UUID getTargetUuid(UUID from)
    {
        return uuidMap.get(Objects.requireNonNull(from, "Original UUID to put into map can't be null."));
    }

    /**
     * Returns the absolute, normalized path to the server root folder.
     *
     * @return the server folder path
     */
    public Path serverFolder()
    {
        return serverFolder;
    }

    /**
     * Returns the path to the world folder resolved from {@code server.properties}.
     *
     * @return the world folder path
     */
    public Path worldFolder()
    {
        return worldFolder;
    }

    /**
     * Returns whether this run converts toward online or offline mode.
     *
     * @return the conversion target
     */
    public ConversionTarget conversionTarget()
    {
        return conversionTarget;
    }

    /**
     * Returns the detected server type, if set by a detection plugin.
     *
     * @return the server type, or {@code null} if not yet detected
     */
    public ServerType serverType()
    {
        return serverType;
    }

    /**
     * Returns the detected world folder structure, if set by a detection plugin.
     *
     * @return the world folder structure, or {@code null} if not yet detected
     */
    public WorldFolderStructure worldFolderStructure()
    {
        return worldFolderStructure;
    }

    /**
     * Returns the detected save file format, if set by a detection plugin.
     *
     * @return the save file format, or {@code null} if not yet detected
     */
    public SaveFileFormat saveFileFormat()
    {
        return saveFileFormat;
    }

    /**
     * Returns the live map of original-to-remapped player UUIDs.
     * <p>
     * Mutations via {@link #putUuidMapping(UUID, UUID)} are visible through this map.
     *
     * @return the UUID remapping map; never {@code null}
     */
    public Map<UUID, UUID> uuidMap()
    {
        return uuidMap;
    }

    /**
     * Sets the detected server type for this conversion run.
     *
     * @param serverType the detected {@link ServerType}
     */
    public void setServerType(ServerType serverType)
    {
        this.serverType = serverType;
    }

    /**
     * Sets the detected world folder structure for this conversion run.
     *
     * @param worldFolderStructure the detected {@link WorldFolderStructure}
     */
    public void setWorldFolderStructure(WorldFolderStructure worldFolderStructure)
    {
        this.worldFolderStructure = worldFolderStructure;
    }

    /**
     * Sets the detected save file format for this conversion run.
     *
     * @param saveFileFormat the detected {@link SaveFileFormat}
     */
    public void setSaveFileFormat(SaveFileFormat saveFileFormat)
    {
        this.saveFileFormat = saveFileFormat;
    }

    /**
     * Returns the parsed CLI arguments for this run.
     *
     * @return the {@link ParsedArguments} used to build this context
     */
    public ParsedArguments parsedArguments()
    {
        return parsedArguments;
    }

    /**
     * Indicates whether an online/offline conversion was requested on the command line.
     *
     * @return {@code true} if a conversion operation was requested; {@code false} otherwise
     */
    public boolean isConversionOperation()
    {
        return parsedArguments.isConversionOperation();
    }

    /**
     * Returns a string representation of this context for debugging.
     *
     * @return a string including folders, conversion target, detection fields, and UUID map
     */
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
