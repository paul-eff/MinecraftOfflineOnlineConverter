package me.pauleff;

import me.pauleff.common.argparse.ArgumentParser;
import me.pauleff.common.argparse.ParseResult;
import me.pauleff.common.argparse.ParsedArguments;
import me.pauleff.common.config.Config;
import me.pauleff.common.handlers.FileHandler;
import me.pauleff.common.handlers.UUIDHandler;
import me.pauleff.converter.ConversionTarget;
import me.pauleff.converter.ConverterV2;
import me.pauleff.converter.PluginOrchestrator;
import me.pauleff.converter.api.PluginContext;
import me.pauleff.detection.MinecraftFlavor;
import me.pauleff.detection.MinecraftFlavorDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.exit;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String VERSION = "3.1.2";
    public static Config config;
    private static ConverterV2 converter;
    private static MinecraftFlavorDetection mfd;

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

        try
        {
            if (parsedArgs.devMode())
            {
                Path path = Paths.get("/Users/paulferlitz/Desktop/Server/Testing_Servers/Vanilla");
                ConversionTarget conversionTarget = parsedArgs.toOnlineMode().orElse(false) ? ConversionTarget.ONLINE : ConversionTarget.OFFLINE;
                PluginContext ctx = new PluginContext(path, path.resolve("world"), conversionTarget);
                ctx.setParsedArguments(parsedArgs);
                PluginOrchestrator orchestrator = new PluginOrchestrator();
                orchestrator.run(ctx);
                LOGGER.info(ctx.toString());
                return;
            } else
            {
                if (parsedArgs.hasPath())
                {
                    Path path = parsedArgs.serverPath().orElseThrow();
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
        {
            argumentParser.printHelp();
            exit(0);
        }


        MinecraftFlavor mcFlavor = mfd.detectMinecraftFlavor();
        LOGGER.info("This is a {} Minecraft Server!", mcFlavor);


        Path serverProperties = converter.serverFolder.resolve("server.properties");
        String oldWorldPath = FileHandler.readWorldNameFromProperties(serverProperties, false);

        String worldName = FileHandler.readWorldNameFromProperties(serverProperties, true);
        converter.setWorldFolder(worldName);

        if (parsedArgs.movePlayerData())
        {
            String movePlayerdataSourceDir = parsedArgs.movePlayerdataSourceDir()
                    .filter(dir -> !dir.isBlank())
                    .orElse(oldWorldPath);
            converter.copyPlayerData(movePlayerdataSourceDir, mcFlavor);
        }

        if (parsedArgs.shouldConvert())
        {

            converter.convert(parsedArgs.toOnlineMode().orElseThrow(), mcFlavor);
        }
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        if (elapsedSeconds > 0.15)
        {
            LOGGER.info("Job finished in {} seconds.", String.format("%.3f", elapsedSeconds));
        }
    }
}
