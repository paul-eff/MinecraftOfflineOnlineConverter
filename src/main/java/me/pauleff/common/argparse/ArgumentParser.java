package me.pauleff.common.argparse;

import me.pauleff.common.LoggerConfigurator;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;


public final class ArgumentParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentParser.class);

    private final String appName;
    private final String version;
    private final Options options;
    private final HelpFormatter formatter;

    public ArgumentParser(String appName, String version)
    {
        this.appName = appName;
        this.version = version;
        this.options = CliOptions.create();
        this.formatter = new HelpFormatter();
    }

    public HelpFormatter formatter()
    {
        return formatter;
    }

    public void printHelp()
    {
        formatter.printHelp(appName, options);
    }

    public ParseResult parse(String[] args)
    {
        try
        {
            CommandLine cmd = new DefaultParser().parse(options, args);
            LoggerConfigurator.configure(cmd.hasOption("verbose"));

            if (cmd.hasOption("h"))
            {
                printHelp();
            }
            if (cmd.hasOption("v"))
            {
                LOGGER.info("{} v{}", appName, version);
            }
            if (cmd.hasOption("v") || cmd.hasOption("h"))
            {
                return ParseResult.exit(0);
            }

            return ParseResult.success(buildArguments(cmd));
        } catch (ParseException e)
        {
            LOGGER.error(e.getMessage());
            printHelp();
            return ParseResult.exit(1);
        }
    }

    private ParsedArguments buildArguments(CommandLine cmd) throws ParseException
    {
        Optional<Boolean> toOnlineMode = Optional.empty();
        if (cmd.hasOption("offline"))
        {
            toOnlineMode = Optional.of(false);
        } else if (cmd.hasOption("online"))
        {
            toOnlineMode = Optional.of(true);
        }

        Optional<Path> serverPath = cmd.hasOption("p")
                ? Optional.of(Paths.get(cmd.getOptionValue("path")))
                : Optional.empty();

        Optional<String> copyPlayerDataSourceWorld = Optional.empty();
        if (cmd.hasOption("copy"))
        {
            String sourceWorld = cmd.getOptionValue("copy");
            if (sourceWorld == null || sourceWorld.isBlank())
            {
                throw new ParseException("Option copy requires a source world name");
            }
            copyPlayerDataSourceWorld = Optional.of(sourceWorld);
        }

        return new ParsedArguments(
                cmd.hasOption("verbose"),
                serverPath,
                toOnlineMode,
                copyPlayerDataSourceWorld,
                parseServerPropertiesChanges(cmd));
    }

    private Map<String, String> parseServerPropertiesChanges(CommandLine cmd)
    {
        if (!cmd.hasOption("properties"))
        {
            return Map.of();
        }

        Properties properties = cmd.getOptionProperties("properties");
        Map<String, String> changes = new HashMap<>();
        for (String key : properties.stringPropertyNames())
        {
            String value = properties.getProperty(key);
            if (value != null)
            {
                changes.put(key, value);
            } else
            {
                LOGGER.error("Missing value for key '{}'", key);
                break;
            }
        }
        return Map.copyOf(changes);
    }
}
