package me.pauleff.common.argparse;

/**
 * Represents the outcome of parsing command-line arguments.
 * <p>
 * Distinguishes between a successful parse that yields {@link ParsedArguments}
 * and a terminal result that should end the process with an exit code.
 *
 * @param arguments the parsed arguments on success, or {@code null} when the process should exit
 * @param exitCode  a process exit code when {@code >= 0}; {@code -1} indicates a successful parse
 */
public record ParseResult(ParsedArguments arguments, int exitCode)
{
    /**
     * Creates a successful parse result that carries the given arguments.
     *
     * @param arguments the parsed arguments
     * @return a result with {@code exitCode} {@code -1}
     */
    public static ParseResult success(ParsedArguments arguments)
    {
        return new ParseResult(arguments, -1);
    }

    /**
     * Creates a terminal parse result that signals the process should exit.
     *
     * @param exitCode the process exit code to use
     * @return a result with {@code null} arguments and the given exit code
     */
    public static ParseResult exit(int exitCode)
    {
        return new ParseResult(null, exitCode);
    }

    /**
     * Indicates whether the caller should terminate instead of continuing with conversion.
     *
     * @return {@code true} if {@code exitCode} is {@code >= 0}; {@code false} otherwise
     */
    public boolean shouldExit()
    {
        return exitCode >= 0;
    }
}
