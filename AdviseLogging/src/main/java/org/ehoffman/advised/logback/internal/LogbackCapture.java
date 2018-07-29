/*
 * The MIT License
 * Copyright © 2016 Rex Hoffman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ehoffman.advised.logback.internal;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
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
    start(null, null);
  }

  /**
   * Start capturing.
   * 
   * @param loggerName
   *          if null, defaults to the root logger
   * @param layoutPattern
   *          if null, defaults to "[%p] %m%n"
   */
  public static void start(final String loggerName, final String layoutPattern) {
    if (INSTANCE.get() != null) {
      throw new IllegalStateException("already started");
    }
    INSTANCE.set(new LogbackCapture(loggerName, layoutPattern));
  }

  /**
   * Stop capturing and return the logs.
   * 
   * @return a String containing all logging that occurred during the test execution on it's thread.
   */
  public static String stop() {
    final LogbackCapture instance = INSTANCE.get();
    if (instance == null) {
      throw new IllegalStateException("was not running");
    }
    final String result = instance.stopInstance();
    INSTANCE.remove();
    return result;
  }

  private LogbackCapture(final String loggerName, final String layoutPattern) {
    logs = new ByteArrayOutputStream(4096);
    encoder = buildEncoder(layoutPattern);
    appender = buildAppender(encoder, logs);
    logger = getLogbackLogger(loggerName);
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

  private static Logger getLogbackLogger(String name) {
    if (name == null || name.isEmpty()) {
      name = ROOT_LOGGER_NAME;
    }
    final Logger logger = getContext().getLogger(name);
    return logger;
  }

  private static Encoder<ILoggingEvent> buildEncoder(String layoutPattern) {
    if (layoutPattern == null) {
      layoutPattern = "[%p] %m%n";
    }
    final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setPattern(layoutPattern);
    encoder.setCharset(Charset.forName("UTF-16"));
    encoder.setContext(getContext());
    encoder.start();
    return encoder;
  }

  private static OutputStreamAppender<ILoggingEvent> buildAppender(final Encoder<ILoggingEvent> encoder,
          final OutputStream outputStream) {
    final OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<ILoggingEvent>();
    appender.setName("logcapture");
    appender.setContext(getContext());
    appender.setEncoder(encoder);
    appender.setOutputStream(outputStream);
    appender.start();
    return appender;
  }

  /**
   * Given http://www.slf4j.org/codes.html#substituteLogger we occasionally need to retry getting the logging context on test's
   * startup
   * 
   * @return the current {@link LoggerContext}.
   * 
   * @throws NullPointerException if the {@link LoggerContext} hasn't been set.
   * 
   * @throws RuntimeException with a cause of {@link InterruptedException} if the thread is interrupted while waiting for the
   *           {@link LoggerContext} to be set.
   */
  private static LoggerContext getContext() {
    return new Wait<LoggerContext>()
            .on(() -> ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext())
            .trying(10)
            .toComplete();
  }
}
