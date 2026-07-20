package me.pauleff.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter2;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LoggerConfigurator
{
    private static final Path LOG_DIR = Path.of("mooc_logs");
    private static final String LOG_FILE_PATTERN = LOG_DIR.getFileName() + "/MinecraftOfflineOnlineConverter-%d{yyyy-MM-dd}.log";
    private static final String LINE_PATTERN = "[%d{yyyy-MM-dd HH:mm:ss}][%-5level][%logger{36}] %msg%n";
    private static final String CONSOLE_PATTERN_VERBOSE = "[%d{HH:mm:ss}][%-5level][%logger{0}] %msg%n";
    private static final String CONSOLE_PATTERN_DEFAULT = "[%d{HH:mm:ss}][%-5level] %msg%n";

    private LoggerConfigurator()
    {
    }

    public static void configure(boolean verbose)
    {
        try
        {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e)
        {
            System.err.println("Could not create logs directory: " + e.getMessage());
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.TRACE);
        rootLogger.addAppender(createConsoleAppender(context, verbose));
        rootLogger.addAppender(createFileAppender(context));

        new StatusPrinter2().printInCaseOfErrorsOrWarnings(context);
    }

    private static Appender<ILoggingEvent> createConsoleAppender(LoggerContext context, boolean verbose)
    {
        PatternLayoutEncoder encoder = createEncoder(
                context,
                verbose ? CONSOLE_PATTERN_VERBOSE : CONSOLE_PATTERN_DEFAULT);

        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(context);
        filter.setLevel(verbose ? Level.DEBUG.levelStr : Level.INFO.levelStr);
        filter.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.addFilter(filter);
        appender.start();
        return appender;
    }

    private static Appender<ILoggingEvent> createFileAppender(LoggerContext context)
    {
        PatternLayoutEncoder encoder = createEncoder(context, LINE_PATTERN);

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(LOG_FILE_PATTERN);

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.setRollingPolicy(rollingPolicy);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.start();
        return appender;
    }

    private static PatternLayoutEncoder createEncoder(LoggerContext context, String pattern)
    {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(pattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        return encoder;
    }
}
