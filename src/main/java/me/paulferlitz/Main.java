package me.paulferlitz;

import me.paulferlitz.minecraftflavours.MinecraftFlavourDetection;
import me.paulferlitz.minecraftflavours.MinecraftFlavour;
import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;

import org.apache.commons.cli.*;

/**
 * Main Class.
 *
 * @author Paul Ferlitz
 */
public class Main
{
    // Class variables
    private static final String version = "BETA 4";

    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static ConverterV2 converter;
    private static MinecraftFlavourDetection mfd;
    private static CommandLine cmd;

    /**
     * Main method and entry point of jar.
     *
     * @param args The arguments passed on execution.
     * @throws PathNotValidException If the given path was not resolvable.
     * @throws InvalidArgumentException If a given argument was not valid.
     */
    public static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();
        System.out.println("\nStarting MinecraftOfflineOnlineConverter Version " + version + "...\n");
        // Argument parsing
        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            }

            if (cmd.hasOption("offline"))
            {
                mode = "-offline";
                if (cmd.hasOption("v")) {
                    System.out.println("Mode set to offline");
                }
            }else if (cmd.hasOption("online"))
            {
                mode = "-online";
                if (cmd.hasOption("v")) {
                    System.out.println("Mode set to online");
                }
            } else {
                throw new ParseException("Neither -offline or -online argument was found. Please specify which mode you want!");
            }

            if (cmd.hasOption("p")) {
                hasPath = true;
                // Instantiate Converter with individual path
                converter = new ConverterV2(cmd.getOptionValue("p"));
                mfd = new MinecraftFlavourDetection(cmd.getOptionValue("p"));
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            System.exit(1);
        }

        // Instantiate Converter with default values
        if (!hasPath)
        {
            converter = new ConverterV2();
            mfd = new MinecraftFlavourDetection();
        }
        /*
         * Held my promise made in commit 375fc63 on Nov 2, 2021.
         */

        // Determine Minecraft Server flavour (e.g. Vanilla,Paper,Forge,...)
        MinecraftFlavour mcFlavour = mfd.detectMinecraftFlavour();
        System.out.println("This is a " + mcFlavour.toString() + " Minecraft Server!");

        // Start conversion
        converter.convert(mode, mcFlavour);

        System.out.println("\nJob finished in " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds.");
    }

    private static Options getOptions()
    {
        Options options = new Options();

        Option path = new Option("p", "path", true, "Path to the server folder");
        options.addOption(path);
        Option verbose = new Option("v", "verbose", false, "Enable verbose output");
        options.addOption(verbose);
        Option help = new Option("h", "help", false, "Get some help");
        options.addOption(help);

        Option offline = new Option("offline", false, "Convert server files to offline mode");
        options.addOption(offline);
        Option online = new Option("online", false, "Convert server files to online mode");
        options.addOption(online);
        return options;
    }

    public static CommandLine getArgs() {
        return cmd;
    }
}
