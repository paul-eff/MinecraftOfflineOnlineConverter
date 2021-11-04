package me.paulferlitz;

import me.paulferlitz.exceptions.InvalidArgumentException;
import me.paulferlitz.exceptions.PathNotValidException;

import java.util.Locale;

public class Main
{
    private static final String version = "2.2.0";

    private static String mode = "N/A";
    private static boolean hasPath = false;
    private static Converter converter;

    public static void main(String[] args) throws PathNotValidException, InvalidArgumentException
    {
        long startTime = System.nanoTime();
        System.out.println("\nStarting MinecraftOfflineOnlineConverter Version " + version + "...\n");

        for (int i = 0; i < args.length; i++)
        {
            switch (args[i].toLowerCase(Locale.ROOT))
            {
                case "-p":
                    hasPath = true;
                    converter = new Converter(args[i + 1]);
                    break;
                case "-offline":
                case "-online":
                    mode = args[i];
                    break;
                default:
                    if (!args[(i - 1) < 0 ? 0 : (i - 1)].equals("-p")) throw new InvalidArgumentException(args[i]);
            }
        }

        if (!hasPath) converter = new Converter();
        /**
         * Held my promise made in commit 375fc63 on Nov 2, 2021.
         */
        converter.convert(mode);

        System.out.println("\nJob finished in " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds.");
    }
}
