package me.pauleff.common.argparse;

import me.pauleff.common.LoggerConfigurator;
import me.pauleff.common.handlers.UUIDHandler;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parses command-line arguments into a {@link ParseResult} for this application.
 * <p>
 * Handles help and version requests as terminal exits, configures logging verbosity,
 * and applies custom UUID API options when present.
 */
public final class ArgumentParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentParser.class);

    private final String appName;
    private final String version;
    private final Options options;
    private final HelpFormatter formatter;
    private final CommandLineParser parser;

    /**
     * Creates a parser for the given application name and version string.
     *
     * @param appName the application name shown in help and version output
     * @param version the application version shown with the {@code -v} flag
     */
    public ArgumentParser(String appName, String version)
    {
        this.appName = appName;
        this.version = version;
        this.options = CliOptions.create();
        this.formatter = new HelpFormatter();
        this.parser = new DefaultParser();
    }

    /**
     * Prints usage help for the configured CLI options to standard output.
     */
    public void printHelp()
    {
        formatter.printHelp(appName, options);
    }

    /**
     * Parses the given command-line arguments and returns a {@link ParseResult}.
     * <p>
     * On success, returns {@link ParseResult#success(ParsedArguments)}. Help or version
     * requests return {@link ParseResult#exit(int)} with code {@code 0}. Parse failures
     * print help and return an exit result with code {@code 1}.
     *
     * @param args the raw command-line arguments
     * @return a successful result with {@link ParsedArguments}, or a terminal exit result
     */
    public ParseResult parse(String[] args)
    {
        try
        {
            CommandLine cmd = parser.parse(options, args);
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

    /**
     * Builds {@link ParsedArguments} from a successfully parsed {@link CommandLine}.
     * <p>
     * Also applies any custom UUID API options to {@link UUIDHandler}.
     *
     * @param cmd the parsed command line
     * @return the structured arguments for conversion and related operations
     * @throws ParseException if a required option value is missing or blank
     */
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

        applyCustomApiOptions(cmd);

        return new ParsedArguments(
                serverPath,
                toOnlineMode,
                copyPlayerDataSourceWorld,
                parseServerPropertiesChanges(cmd));
    }

    /**
     * Applies custom UUID API URL options from the command line to {@link UUIDHandler}.
     * <p>
     * Blank values are ignored with a warning; Mojang defaults remain in use when unset.
     *
     * @param cmd the parsed command line
     */
    private void applyCustomApiOptions(CommandLine cmd)
    {
        if (cmd.hasOption("customApiBaseUrl"))
        {
            String customApiBaseUrl = cmd.getOptionValue("customApiBaseUrl");
            if (customApiBaseUrl != null && !customApiBaseUrl.isBlank())
            {
                UUIDHandler.setCustomApiBaseUrl(customApiBaseUrl);
            } else
            {
                LOGGER.warn("Option -customApiBaseUrl was set without a URL. Using Mojang defaults.");
            }
        }

        if (cmd.hasOption("retrieveUUIDUrl"))
        {
            String retrieveUUIDUrl = cmd.getOptionValue("retrieveUUIDUrl");
            if (retrieveUUIDUrl != null && !retrieveUUIDUrl.isBlank())
            {
                UUIDHandler.setRetrieveUUIDUrl(retrieveUUIDUrl);
            } else
            {
                LOGGER.warn("Option -retrieveUUIDUrl was set without a URL. Ignoring.");
            }
        }

        if (cmd.hasOption("retrieveNameUrl"))
        {
            String retrieveNameUrl = cmd.getOptionValue("retrieveNameUrl");
            if (retrieveNameUrl != null && !retrieveNameUrl.isBlank())
            {
                UUIDHandler.setRetrieveNameUrl(retrieveNameUrl);
            } else
            {
                LOGGER.warn("Option -retrieveNameUrl was set without a URL. Ignoring.");
            }
        }
    }

    /**
     * Parses {@code -properties} key/value pairs into an immutable map of server property changes.
     *
     * @param cmd the parsed command line
     * @return the property changes to apply, or an empty map when the option is absent
     */
    private Map<String, String> parseServerPropertiesChanges(CommandLine cmd)
    {
        if (!cmd.hasOption("properties"))
        {
            return Map.of();
        }

        // Pair flattened [key, value, ...] from getOptionValues; getOptionProperties only keeps the first pair.
        String[] values = cmd.getOptionValues("properties");
        Map<String, String> changes = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            changes.put(values[i], values[i + 1]);
        }
        return Map.copyOf(changes);
    }
}
