package me.pauleff.common;

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

public class LoggerConfigurator
{

    public static void configure(boolean verbose)
    {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        String consolePattern = verbose
                ? "[%d{HH:mm:ss}][%-5level][%logger{0}] - %msg%n"
                : "[%d{HH:mm:ss}][%-5level] - %msg%n";
        consoleEncoder.setPattern(consolePattern);
        consoleEncoder.setCharset(StandardCharsets.UTF_8);
        consoleEncoder.start();

        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setContext(context);
        consoleFilter.setLevel(verbose ? Level.DEBUG.levelStr : Level.INFO.levelStr);
        consoleFilter.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.addFilter(consoleFilter);
        consoleAppender.start();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}][%-5level][%logger{36}] - %msg%n");
        fileEncoder.setCharset(StandardCharsets.UTF_8);
        fileEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setFile("MinecraftOfflineOnlineConverter.log");
        fileAppender.setAppend(true);
        fileAppender.start();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.TRACE); // Always capture everything for file
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

    }
}
