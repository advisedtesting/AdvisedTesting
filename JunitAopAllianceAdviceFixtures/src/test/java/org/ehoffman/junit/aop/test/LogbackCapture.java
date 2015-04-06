/*
 * #%L
 * JunitAopAllianceAdviceFixtures
 * %%
 * Copyright (C) 2015 Rex Hoffman
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.ehoffman.junit.aop.test;

import static ch.qos.logback.classic.Level.ALL;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;

/**
 * Temporarily captures Logback output (mostly useful for tests). Based on https://gist.github.com/olim7t/881318.
 */
public class LogbackCapture {

    private static final ThreadLocal<LogbackCapture> INSTANCE = new ThreadLocal<LogbackCapture>();

    private final Logger logger;
    private final OutputStreamAppender<ILoggingEvent> appender;
    private final Encoder<ILoggingEvent> encoder;
    private final ByteArrayOutputStream logs;

    public static void start() {
        if (INSTANCE.get() != null) {
            throw new IllegalStateException("already started");
        }
        INSTANCE.set(new LogbackCapture(null, null, null));
    }

    /**
     * Start capturing.
     * 
     * @param loggerName
     *            if null, defaults to the root logger
     * @param level
     *            if null, defaults to all levels
     * @param layoutPattern
     *            if null, defaults to "[%p] %m%n"
     */
    public static void start(final String loggerName, final Level level, final String layoutPattern) {
        if (INSTANCE.get() != null) {
            throw new IllegalStateException("already started");
        }
        INSTANCE.set(new LogbackCapture(loggerName, level, layoutPattern));
    }

    /** Stop capturing and return the logs. */
    public static String stop() {
        final LogbackCapture instance = INSTANCE.get();
        if (instance == null) {
            throw new IllegalStateException("was not running");
        }
        final String result = instance.stopInstance();
        INSTANCE.remove();
        return result;
    }

    private LogbackCapture(final String loggerName, final Level level, final String layoutPattern) {
        logs = new ByteArrayOutputStream(4096);
        encoder = buildEncoder(layoutPattern);
        appender = buildAppender(encoder, logs);
        logger = getLogbackLogger(loggerName, level);
        logger.addAppender(appender);
    }

    private String stopInstance() {
        appender.stop();
        try {
            return logs.toString("UTF-16");
        } catch (final UnsupportedEncodingException cantHappen) {
            return null;
        }
    }

    private static Logger getLogbackLogger(String name, Level level) {
        if (name == null || name.isEmpty()) {
            name = ROOT_LOGGER_NAME;
        }
        if (level == null) {
            level = ALL;
        }

        final Logger logger = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext()
                        .getLogger(name);
        logger.setLevel(level);
        return logger;
    }

    private static Encoder<ILoggingEvent> buildEncoder(String layoutPattern) {
        if (layoutPattern == null) {
            layoutPattern = "[%p] %m%n";
        }
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(layoutPattern);
        encoder.setCharset(Charset.forName("UTF-16"));
        encoder.setContext(ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext());
        encoder.start();
        return encoder;
    }

    private static OutputStreamAppender<ILoggingEvent> buildAppender(final Encoder<ILoggingEvent> encoder,
                    final OutputStream outputStream) {
        final OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<ILoggingEvent>();
        appender.setName("logcapture");
        appender.setContext(ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext());
        appender.setEncoder(encoder);
        appender.setOutputStream(outputStream);
        appender.start();
        return appender;
    }
}
