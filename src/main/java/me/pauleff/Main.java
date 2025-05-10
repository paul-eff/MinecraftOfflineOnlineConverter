package me.pauleff;

import me.pauleff.exceptions.PathNotValidException;
import me.pauleff.handlers.LoggerConfigurator;
import me.pauleff.minecraftflavors.MinecraftFlavor;
import me.pauleff.minecraftflavors.MinecraftFlavorDetection;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

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
    private static final String VERSION = "3";
    private static CommandLine cmd;
    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static ConverterV2 converter;
    private static MinecraftFlavorDetection mfd;

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
        parseArguments(args, options);
        LOGGER.info("Starting MinecraftOfflineOnlineConverter Version {}", VERSION);
        // Setup converter
        initializeConverter();
        // Detect Minecraft server flavor
        MinecraftFlavor mcFlavor = mfd.detectMinecraftFlavor();
        LOGGER.info("This is a {} Minecraft Server!", mcFlavor);
        // Start conversion process
        converter.convert(mode, mcFlavor);
        LOGGER.info("Job finished in {} seconds.", String.format("%.3f", (System.nanoTime() - startTime) / 1_000_000_000.0));
    }

    /**
     * Defines available command-line options.
     *
     * @return Configured Options object.
     */
    private static Options defineOptions()
    {
        Options options = new Options();
        options.addOption("p", "path", true, "Path to the server folder");
        options.addOption("v", "verbose", false, "Enable verbose output");
        options.addOption("h", "help", false, "Display help message");
        options.addOption("offline", false, "Convert server files to offline mode");
        options.addOption("online", false, "Convert server files to online mode");
        return options;
    }

    /**
     * Parses command-line arguments and handles invalid inputs.
     *
     * @param args    The command-line arguments.
     * @param options The available command-line options.
     */
    private static void parseArguments(String[] args, Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        try
        {
            cmd = new DefaultParser().parse(options, args);
            // Configure logger for the whole application
            LoggerConfigurator.configure(cmd.hasOption("v"));
            // Handle printing the help message
            if (cmd.hasOption("h"))
            {
                formatter.printHelp("MinecraftOfflineOnlineConverter", options);
                exit(0);
            }
            // Handle setting the mode to convert to
            if (cmd.hasOption("offline"))
            {
                mode = "-offline";
            } else if (cmd.hasOption("online"))
            {
                mode = "-online";
            } else
            {
                throw new ParseException("Specify either -offline or -online mode.");
            }
            // Set if a path is provided
            hasPath = cmd.hasOption("p");
        } catch (ParseException e)
        {
            // If there was any error, print it and display the help message
            LOGGER.error(e.getMessage());
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            exit(1);
        }
    }

    /**
     * Initializes the converter and Minecraft flavor detection based on parsed arguments.
     */
    private static void initializeConverter() throws PathNotValidException
    {
        // Init converter with or without a path to the server folder and detect the Minecraft flavor
        if (hasPath)
        {
            String path = cmd.getOptionValue("p");
            converter = new ConverterV2(Path.of(path));
            mfd = new MinecraftFlavorDetection(path);
        } else
        {
            converter = new ConverterV2();
            mfd = new MinecraftFlavorDetection();
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