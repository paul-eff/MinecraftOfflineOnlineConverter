package me.paulferlitz;

import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;

import java.util.Locale;

import org.apache.commons.cli.*;

/**
 * Main Class.
 *
 * @author Paul Ferlitz
 */
public class Main
{
    // Class variables
    private static final String version = "2.3.4";

    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static Converter converter;

    /**
     * Main method and entry point of jar.
     *
     * @param args The arguments passed on execution.
     * @throws PathNotValidException If the given path was not resolvable.
     * @throws InvalidArgumentException If a given argument was not valid.
     */
    public static void main(String[] args) throws PathNotValidException, InvalidArgumentException
    {
        long startTime = System.nanoTime();
        System.out.println("\nStarting MinecraftOfflineOnlineConverter Version " + version + "...\n");
        // Argument parsing
        Options options = new Options();

        Option input = new Option("p", "path", true, "Path to the server folder");
        options.addOption(input);

        Option offline = new Option("offline", false, "Convert server files to offline mode");
        options.addOption(offline);
        Option online = new Option("online", false, "Convert server files to online mode");
        options.addOption(online);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            }

            if (cmd.hasOption("offline"))
            {
                mode = "-offline";
            }else if (cmd.hasOption("online"))
            {
                mode = "-online";
            } else {
                throw new ParseException("Neither -offline or -online argument was found. Please specify which mode you want!");
            }

            if (cmd.hasOption("p")) {
                hasPath = true;
                // Instantiate Converter with individual path
                converter = new Converter(cmd.getOptionValue("p"));
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MinecraftOfflineOnlineConverter", options);
            System.exit(1);
        }

        // Instantiate Converter with default values
        if (!hasPath) converter = new Converter();
        /*
         * Held my promise made in commit 375fc63 on Nov 2, 2021.
         */
        // Start conversion
        converter.convert(mode);

        System.out.println("\nJob finished in " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds.");
    }
}
