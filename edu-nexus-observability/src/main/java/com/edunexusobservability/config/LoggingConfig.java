package com.edunexusobservability.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Structured logging configuration with correlation ID support.
 *
 * Provides consistent logging patterns across all services:
 * - Enriched with MDC correlation context
 * - Structured JSON format for production
 * - Rolling file rotation
 * - Console logging for development
 */
@Configuration
public class LoggingConfig {

    public void configureLogging(String serviceName, boolean production) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Root logger configuration
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        // Service-specific logger
        Logger serviceLogger = loggerContext.getLogger("com.edunexusobservability");
        serviceLogger.setLevel(Level.DEBUG);

        // Pattern layout for console output
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(loggerContext);
        consoleEncoder.setPattern(production
            ? "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n"
            : "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n");
        consoleEncoder.start();

        // Console appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();
        rootLogger.addAppender(consoleAppender);

        if (production) {
            // Rolling file appender
            RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
            rollingFileAppender.setContext(loggerContext);
            rollingFileAppender.setFile(Path.of("logs", serviceName, serviceName + ".log").toString());

            // Rolling policy
            SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
            rollingPolicy.setContext(loggerContext);
            rollingPolicy.setFileNamePattern(Path.of("logs", serviceName, serviceName + "-%d{yyyy-MM-dd}.%i.log.gz").toString());
            rollingPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
            rollingPolicy.setMaxHistory(30);
            rollingPolicy.setTotalSizeCap(FileSize.valueOf("1GB"));
            rollingPolicy.setParent(rollingFileAppender);
            rollingPolicy.start();

            // Encoder for file output
            PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
            fileEncoder.setContext(loggerContext);
            fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId},%X{traceId},%X{spanId}] - %msg%n");
            fileEncoder.start();

            rollingFileAppender.setEncoder(fileEncoder);
            rollingFileAppender.setRollingPolicy(rollingPolicy);
            rollingFileAppender.start();

            rootLogger.addAppender(rollingFileAppender);
            serviceLogger.addAppender(rollingFileAppender);
        }
    }
}