package me.pauleff.common.argparse;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Defines the CLI options supported by this application.
 */
public final class CliOptions
{
    private CliOptions()
    {
    }

    /**
     * Builds and returns the configured CLI options for argument parsing.
     *
     * @return a non-{@code null} {@link Options} instance containing all supported flags and arguments
     */
    public static Options create()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "Display help message");

        options.addOption("p", "path", true, "Target server root folder (default: current terminal working directory)");

        Option customFolder = Option.builder("customFolder")
                .desc("Convert only the content of this custom folder. Takes an absolute path or relative to the working directory")
                .hasArg()
                .argName("path")
                .build();
        options.addOption(customFolder);

        Option customFile = Option.builder("customFile")
                .desc("Convert only this file. Takes an absolute path or relative to the working directory")
                .hasArg()
                .argName("path")
                .build();
        options.addOption(customFile);
        options.addOption("includeServerFiles", false,
                "Also convert default server files. Requires -customFolder or -customFile");

        options.addOption("v", false, "Print version");
        options.addOption("verbose", false, "Enable verbose (debug) console output");
        options.addOption("offline", false, "Convert server files to offline mode");
        options.addOption("online", false, "Convert server files to online mode");

        Option copyOption = Option.builder("copy")
                .desc("Copy player data from the specified source world folder to the current world")
                .hasArg()
                .argName("world-name")
                .build();
        options.addOption(copyOption);

        Option properties = Option.builder("properties")
                .hasArgs()
                .valueSeparator('=')
                .build();
        properties.setDescription("Edit server.properties using -properties key1=value1 key2=value2");
        options.addOption(properties);

        Option customApiBaseUrl = Option.builder("customApiBaseUrl")
                .desc("Set the API base URL, such as https://myskinserver.com. MOOC appends the Mojang path and player name or UUID")
                .hasArg()
                .argName("url")
                .build();
        options.addOption(customApiBaseUrl);

        Option retrieveUUIDUrl = Option.builder("retrieveUUIDUrl")
                .desc("Full name to UUID endpoint URL (domain + path). Only the player name is appended. Overrides -customApiBaseUrl")
                .hasArg()
                .argName("url")
                .build();
        options.addOption(retrieveUUIDUrl);

        Option retrieveNameUrl = Option.builder("retrieveNameUrl")
                .desc("Full UUID to name endpoint URL (domain + path). Only the UUID is appended. Overrides -customApiBaseUrl")
                .hasArg()
                .argName("url")
                .build();
        options.addOption(retrieveNameUrl);

        return options;
    }
}
