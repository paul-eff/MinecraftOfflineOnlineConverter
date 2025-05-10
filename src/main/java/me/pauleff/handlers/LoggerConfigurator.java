package me.pauleff.handlers;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * LoggerConfigurator is responsible for configuring the logging system using Logback.
 */
public class LoggerConfigurator {

    /**
     * Configures the logging system.
     * Sets up console and file appenders with specified patterns and encoders.
     *
     * @param verbose If true, sets the logging level to DEBUG; otherwise, sets it to INFO.
     */
    public static void configure(boolean verbose) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // Console encoder
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        consoleEncoder.setPattern("[%d{HH:mm:ss}][%-5level] - %msg%n");
        consoleEncoder.setCharset(StandardCharsets.UTF_8);
        consoleEncoder.start();

        // Console appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();

        // File encoder
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}][%-5level][%logger{36}] - %msg%n");
        fileEncoder.setCharset(StandardCharsets.UTF_8);
        fileEncoder.start();

        // File appender
        // TODO: Think about tiering the logs, splitting them per day/week/month, deleting after x days, ...
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setFile("MinecraftOfflineOnlineConverter.log");
        fileAppender.setAppend(true);
        fileAppender.start();

        // Root logger setup
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(verbose ? Level.DEBUG : Level.INFO);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        // Print any Logback config issues
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
}
