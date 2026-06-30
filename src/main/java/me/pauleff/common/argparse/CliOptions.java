package me.pauleff.common.argparse;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public final class CliOptions
{
    private CliOptions()
    {
    }

    public static Options create()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "Display help message");

        options.addOption("p", "path", true, "Specify path to the server folder");
        options.addOption("v", false, "Print version");
        options.addOption("verbose", false, "Enable verbose output");
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
        properties.setDescription("Edit values in server.properties; (i.e., -properties key1=value1 key2=value2)");
        options.addOption(properties);

        return options;
    }
}
