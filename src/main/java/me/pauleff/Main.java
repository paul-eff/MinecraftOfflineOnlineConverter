package me.pauleff;

import me.pauleff.common.LoggerConfigurator;
import me.pauleff.common.config.Config;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.converter.ConverterV2;
import me.pauleff.converter.PluginOrchestrator;
import me.pauleff.converter.PluginRegistry;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.detection.MinecraftFlavor;
import me.pauleff.detection.MinecraftFlavorDetection;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.exit;

/**
 * Entry point for MinecraftOfflineOnlineConverter.
 * Handles argument parsing, initializes components, and starts the conversion process.
 *
 * @author Paul Ferlitz
 */
public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String VERSION = "3.1.1";
    private static CommandLine cmd;
    private static final HashMap<String, String> serverPropertiesChanges = new HashMap<>();
    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static boolean isDev = false;
    private static ConverterV2 converter;
    private static MinecraftFlavorDetection mfd;
    private static boolean movePlayerData = false;
    private static String movePlayerdataSourceDir = null;
    public static Config config;

    /**
     * Main method - entry point of the application.
     * Parses arguments, initializes converter, and executes the conversion.
     *
     * @param args The command-line arguments.
     * @throws Exception If any error occurs during execution.
     */
    public static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();
        // Setup CLI options and logger
        Options options = defineOptions();
        HelpFormatter formatter = new HelpFormatter();
        parseArguments(args, formatter, options);
        LOGGER.debug("Starting MinecraftOfflineOnlineConverter Version {}", VERSION);

        try
        {// Init converter with or without a path to the server folder and detect the Minecraft flavor
            if (isDev)
            {
                Path path = Paths.get("/Users/paulferlitz/Desktop/Server/Testing_Servers/Vanilla");
                mfd = new MinecraftFlavorDetection(path);
                PluginContext ctx = new PluginContext(path, path.resolve("world"), mode.equals("-online"), mfd.detectMinecraftFlavor(), new HashMap<>());
                PluginRegistry registry = new PluginRegistry(PluginRegistry.defaultPlugins());
                PluginOrchestrator orchestrator = new PluginOrchestrator(registry);
                orchestrator.run(ctx);
                return;
            } else
            {
                if (hasPath)
                {
                    Path path = Paths.get(getArgs().getOptionValue("path"));
                    converter = new ConverterV2(path);
                    mfd = new MinecraftFlavorDetection(path);
                } else
                {
                    converter = new ConverterV2();
                    mfd = new MinecraftFlavorDetection();
                }
                config = new Config(converter.serverFolder);
            }
        } catch (Exception e)
        {//Display help message if the user didn't set something right
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            exit(0);
        }

        // Detect Minecraft server flavor
        MinecraftFlavor mcFlavor = mfd.detectMinecraftFlavor();
        LOGGER.info("This is a {} Minecraft Server!", mcFlavor);

        //Update server.properties
        Path serverProperties = converter.serverFolder.resolve("server.properties");
        String oldWorldPath = FileHandler.readWorldNameFromProperties(serverProperties, false);

        for (Map.Entry<String, String> m : serverPropertiesChanges.entrySet())
        {
            FileHandler.writeToProperties(serverProperties, m.getKey(), m.getValue());
        }

        String worldName = FileHandler.readWorldNameFromProperties(serverProperties, true);
        converter.setWorldFolder(worldName);

        if (movePlayerData)
        {
            if (movePlayerdataSourceDir == null || movePlayerdataSourceDir.isBlank())
                movePlayerdataSourceDir = oldWorldPath;
            converter.copyPlayerData(movePlayerdataSourceDir, mcFlavor);
        }

        if (mode.equals("-online") || mode.equals("-offline"))
        {
            // Start conversion process
            converter.convert(mode.equals("-online"), mcFlavor);
        }
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        if (elapsedSeconds > 0.15)
        {
            LOGGER.info("Job finished in {} seconds.", String.format("%.3f", elapsedSeconds));
        }
    }

    /**
     * Defines available command-line options.
     *
     * @return Configured Options object.
     */
    private static Options defineOptions()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "Display help message");

        options.addOption("d", "Dev", false, "Ignores all other inputs and runs whatever is currently being tested");
        options.addOption("p", "path", true, "Specify path to the server folder");
        options.addOption("v", false, "Print version");
        options.addOption("verbose", false, "Enable verbose output");
        options.addOption("offline", false, "Convert server files to offline mode");
        options.addOption("online", false, "Convert server files to online mode");

        Option copyOption = Option.builder("c")
                .longOpt("copy")
                .desc("Copy player data between worlds; Specify a source folder to copy from (will copy from previous world if unspecified)")
                .optionalArg(true) // This makes the "/path/to/data" part optional
                .hasArg()          // It can still take an argument
                .build();
        options.addOption(copyOption);

        Option properties = Option.builder("properties")
                .hasArgs()           // This is crucial: it tells the parser to expect more than one value
                .valueSeparator('=') // This tells the parser how to split the key from the value
                .build();
        properties.setDescription("Edit values in server.properties; (i.e., -properties key1=value1 key2=value2)");
        options.addOption(properties);
        return options;
    }

    /**
     * Parses command-line arguments and handles invalid inputs.
     *
     * @param args    The command-line arguments.
     * @param options The available command-line options.
     */
    private static void parseArguments(String[] args, HelpFormatter formatter, Options options)
    {
        try
        {
            cmd = new DefaultParser().parse(options, args);
            // Configure logger for the whole application
            LoggerConfigurator.configure(getArgs().hasOption("verbose"));

            if (getArgs().hasOption("v"))
            {
                LOGGER.info("MinecraftOfflineOnlineConverter v{}", VERSION);
            }
            if (getArgs().hasOption("h"))
            {
                formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            }
            if (getArgs().hasOption("v") || getArgs().hasOption("h"))
            {
                exit(0);
            }

            // Handle setting the mode to convert to
            if (getArgs().hasOption("offline"))
            {
                mode = "-offline";
            } else if (getArgs().hasOption("online"))
            {
                mode = "-online";
            }

            if (getArgs().hasOption("d"))
            {
                isDev = true;
                return;
            }

            // Set if a path is provided
            hasPath = getArgs().hasOption("p");

            if (getArgs().hasOption("c"))
            {
                movePlayerData = true;
                movePlayerdataSourceDir = getArgs().getOptionValue("c");
            }

            //Handle server.properties entry changes
            if (getArgs().hasOption("properties"))
            {
                Properties properties = getArgs().getOptionProperties("properties");
                for (String key : properties.stringPropertyNames())
                {
                    String value = properties.getProperty(key);
                    if (value != null) serverPropertiesChanges.put(key, value);
                    else
                    {
                        LOGGER.error("Missing value for key '{}'", key);
                        break;
                    }
                }
            }
        } catch (ParseException e)
        {
            // If there was any error, print it and display the help message
            LOGGER.error(e.getMessage());
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            exit(1);
        }


    }


    /**
     * Retrieves parsed command-line arguments.
     *
     * @return Parsed CommandLine object.
     */
    public static CommandLine getArgs()
    {
        return cmd;
    }
}