package me.pauleff.common.argparse;

public final class ParseResult
{
    private final ParsedArguments arguments;
    private final int exitCode;

    private ParseResult(ParsedArguments arguments, int exitCode)
    {
        this.arguments = arguments;
        this.exitCode = exitCode;
    }

    public static ParseResult success(ParsedArguments arguments)
    {
        return new ParseResult(arguments, -1);
    }

    public static ParseResult exit(int exitCode)
    {
        return new ParseResult(null, exitCode);
    }

    public boolean shouldExit()
    {
        return exitCode >= 0;
    }

    public ParsedArguments arguments()
    {
        return arguments;
    }

    public int exitCode()
    {
        return exitCode;
    }
}
