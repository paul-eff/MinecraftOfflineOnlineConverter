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

        options.addOption("d", "Dev", false, "Ignores all other inputs and runs whatever is currently in development/alpha");
        options.addOption("p", "path", true, "Specify path to the server folder");
        options.addOption("v", false, "Print version");
        options.addOption("verbose", false, "Enable verbose output");
        options.addOption("offline", false, "Convert server files to offline mode");
        options.addOption("online", false, "Convert server files to online mode");

        Option copyOption = Option.builder("c")
                .longOpt("copy")
                .desc("Copy player data between worlds; Specify a source folder to copy from (will copy from previous world if unspecified)")
                .optionalArg(true)
                .hasArg()
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
