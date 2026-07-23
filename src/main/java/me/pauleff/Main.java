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

public final class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String APP_NAME = "MinecraftOfflineOnlineConverter";
    private static final String VERSION = "26.1";

    static void main(String[] args)
    {
        long startTime = System.nanoTime();
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

        logElapsedTime(startTime);
    }

    private static void fail(ArgumentParser argumentParser, String message)
    {
        LOGGER.error(message);
        argumentParser.printHelp();
        exit(1);
    }

    private static void logElapsedTime(long startTime)
    {
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        if (elapsedSeconds > 0.15)
        {
            LOGGER.info("Job finished in {} seconds.", String.format("%.3f", elapsedSeconds));
        }
    }
}
