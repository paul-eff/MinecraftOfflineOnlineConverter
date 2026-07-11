package me.pauleff;

import me.pauleff.common.argparse.ArgumentParser;
import me.pauleff.common.argparse.ParseResult;
import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.exceptions.PathNotValidException;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.ConverterV2;
import me.pauleff.converter.PluginOrchestrator;
import me.pauleff.converter.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String VERSION = "26.1";

    static void main(String[] args) throws Exception
    {
        long startTime = System.nanoTime();
        ArgumentParser argumentParser = new ArgumentParser("MinecraftOfflineOnlineConverter", VERSION);
        ParseResult parseResult = argumentParser.parse(args);
        if (parseResult.shouldExit())
        {
            exit(parseResult.exitCode());
        }

        ParsedArguments parsedArgs = parseResult.arguments();
        LOGGER.info("Starting MinecraftOfflineOnlineConverter Version {}", VERSION);

        if (!parsedArgs.isConversionOperation()
                && !parsedArgs.shouldCopyPlayerData()
                && parsedArgs.serverPropertiesChanges().isEmpty())
        {
            LOGGER.error("No action specified.");
            argumentParser.printHelp();
            exit(1);
        }

        try
        {
            PluginContext ctx = PluginContext.from(parsedArgs);
            new PluginOrchestrator().run(ctx);
        } catch (PathNotValidException e)
        {
            LOGGER.error(e.getMessage());
            argumentParser.printHelp();
            exit(1);
        }

        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        if (elapsedSeconds > 0.15)
        {
            LOGGER.info("Job finished in {} seconds.", String.format("%.3f", elapsedSeconds));
        }
    }
}
