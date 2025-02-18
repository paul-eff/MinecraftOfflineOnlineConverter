package me.paulferlitz;

import me.paulferlitz.minecraftflavours.MinecraftFlavourDetection;
import me.paulferlitz.minecraftflavours.MinecraftFlavour;
import me.paulferlitz.exceptions.PathNotValidException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Entry point for MinecraftOfflineOnlineConverter.
 * Handles argument parsing, initializes components, and starts the conversion process.
 *
 * @author Paul Ferlitz
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String VERSION = "3 BETA 2";
    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static ConverterV2 converter;
    private static MinecraftFlavourDetection mfd;
    private static CommandLine cmd;

    /**
     * Main method - entry point of the application.
     * Parses arguments, initializes converter, and executes the conversion.
     *
     * @param args The command-line arguments.
     * @throws Exception If any error occurs during execution.
     */
    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        logger.info("Starting MinecraftOfflineOnlineConverter Version {}...", VERSION);

        Options options = defineOptions();
        parseArguments(args, options);
        initializeConverter();

        // Detect Minecraft Server Flavour
        MinecraftFlavour mcFlavour = mfd.detectMinecraftFlavour();
        logger.info("This is a {} Minecraft Server!", mcFlavour);

        // Start conversion process
        converter.convert(mode, mcFlavour);
        logger.info("Job finished in {} milliseconds.", (System.nanoTime() - startTime) / 1_000_000);
    }

    /**
     * Defines available command-line options.
     *
     * @return Configured Options object.
     */
    private static Options defineOptions() {
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
    private static void parseArguments(String[] args, Options options) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("MinecraftOfflineOnlineConverter", options);
                System.exit(0);
            }

            if (cmd.hasOption("offline")) {
                mode = "-offline";
            } else if (cmd.hasOption("online")) {
                mode = "-online";
            } else {
                throw new ParseException("Specify either -offline or -online mode.");
            }

            hasPath = cmd.hasOption("p");
        } catch (ParseException e) {
            logger.error("Error: {}", e.getMessage());
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            System.exit(1);
        }
    }

    /**
     * Initializes the converter and Minecraft flavour detection based on parsed arguments.
     */
    private static void initializeConverter() throws PathNotValidException
    {
        if (hasPath) {
            String path = cmd.getOptionValue("p");
            converter = new ConverterV2(Path.of(path));
            mfd = new MinecraftFlavourDetection(path);
        } else {
            converter = new ConverterV2();
            mfd = new MinecraftFlavourDetection();
        }
    }

    /**
     * Retrieves parsed command-line arguments.
     *
     * @return Parsed CommandLine object.
     */
    public static CommandLine getArgs() {
        return cmd;
    }
}