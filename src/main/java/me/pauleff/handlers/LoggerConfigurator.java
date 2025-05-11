package me.pauleff.handlers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * LoggerConfigurator is responsible for configuring the logging system using Logback.
 *
 * @author Paul Ferlitz
 */
public class LoggerConfigurator
{

    /**
     * Configures the logging system.
     * Sets up console and file appenders with specific patterns.
     *
     * @param verbose If true, sets the logging level to DEBUG; otherwise, sets it to INFO.
     */
    public static void configure(boolean verbose)
    {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // Console encoder with dynamic pattern
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        String consolePattern = verbose
                ? "[%d{HH:mm:ss}][%-5level][%logger{0}] - %msg%n"
                : "[%d{HH:mm:ss}][%-5level] - %msg%n";
        consoleEncoder.setPattern(consolePattern);
        consoleEncoder.setCharset(StandardCharsets.UTF_8);
        consoleEncoder.start();

        // Console filter to control minimum level based on 'verbose'
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setContext(context);
        consoleFilter.setLevel(verbose ? Level.DEBUG.levelStr : Level.INFO.levelStr);
        consoleFilter.start();

        // Console appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.addFilter(consoleFilter);
        consoleAppender.start();

        // File encoder
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}][%-5level][%logger{36}] - %msg%n");
        fileEncoder.setCharset(StandardCharsets.UTF_8);
        fileEncoder.start();

        // File appender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setFile("MinecraftOfflineOnlineConverter.log");
        fileAppender.setAppend(true);
        fileAppender.start();

        // Root logger setup
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.TRACE); // Always capture everything for file
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        // Print any Logback config issues
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

    }
}
