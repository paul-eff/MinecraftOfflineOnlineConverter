package me.pauleff;

import me.pauleff.common.argparse.ArgumentParser;
import me.pauleff.common.argparse.ParseResult;
import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.exceptions.PathNotValidException;
import me.pauleff.common.exceptions.UnknownWorldFolderStructureException;
import me.pauleff.converter.PluginOrchestrator;
import me.pauleff.converter.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

/**
 * Application entry point that parses CLI arguments and runs the conversion plugins.
 */
public final class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String APP_NAME = "MinecraftOfflineOnlineConverter";
    private static final String VERSION = "26.1";

    /**
     * Parses command-line arguments and executes the requested conversion operations.
     * <p>
     * Exits early when {@link ParseResult#shouldExit()} is {@code true}. Requires at least
     * one of conversion, player-data copy, or server-properties changes. On path or world-folder
     * structure errors, logs the failure, prints help, and exits with code {@code 1}.
     *
     * @param args the raw command-line arguments
     */
    static void main(String[] args)
    {
        ArgumentParser argumentParser = new ArgumentParser(APP_NAME, VERSION);
        ParseResult parseResult = argumentParser.parse(args);
        if (parseResult.shouldExit())
        {
            exit(parseResult.exitCode());
        }

        ParsedArguments parsedArgs = parseResult.arguments();
        LOGGER.info("Starting {} Version {}", APP_NAME, VERSION);

        if (!parsedArgs.isConversionOperation()
                && !parsedArgs.shouldCopyPlayerData()
                && parsedArgs.serverPropertiesChanges().isEmpty())
        {
            fail(argumentParser, "No action specified.");
        }

        try
        {
            PluginContext ctx = PluginContext.from(parsedArgs);
            new PluginOrchestrator().run(ctx);
        } catch (PathNotValidException | UnknownWorldFolderStructureException e)
        {
            fail(argumentParser, e.getMessage());
        }
    }

    /**
     * Logs an error, prints CLI help, and terminates the process with exit code {@code 1}.
     *
     * @param argumentParser the parser used to print help
     * @param message        the error message to log
     */
    private static void fail(ArgumentParser argumentParser, String message)
    {
        LOGGER.error(message);
        argumentParser.printHelp();
        exit(1);
    }
}
