package me.paulferlitz;

import me.paulferlitz.handlers.CustomPathParser;
import me.paulferlitz.minecraftflavours.MinecraftFlavourDetection;
import me.paulferlitz.minecraftflavours.MinecraftFlavour;
import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;

import java.util.Locale;

/**
 * Main Class.
 *
 * @author Paul Ferlitz
 */
public class Main
{
    // Class variables
    private static final String version = "2.3.3";

    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static ConverterV2 converter;
    private static MinecraftFlavourDetection mfd;

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
        // Iterate over arguments and try to parse them
        for (int i = 0; i < args.length; i++)
        {
            switch (args[i].toLowerCase(Locale.ROOT))
            {
                // On -p parse path
                case "-p":
                    hasPath = true;
                    // Instantiate Converter with individual path
                    converter = new ConverterV2(args[i + 1]);
                    mfd = new MinecraftFlavourDetection(args[i + 1]);
                    break;
                // On -offline / -online set mode
                case "-offline":
                case "-online":
                    mode = args[i];
                    break;
                // In any other occasion check if argument is given path or throw exception
                default:
                    if (!args[(i - 1) < 0 ? 0 : (i - 1)].equals("-p")) throw new InvalidArgumentException(args[i]);
            }
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
}
