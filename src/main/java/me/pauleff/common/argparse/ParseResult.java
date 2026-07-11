package me.pauleff.common.argparse;

public record ParseResult(ParsedArguments arguments, int exitCode)
{
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
}
