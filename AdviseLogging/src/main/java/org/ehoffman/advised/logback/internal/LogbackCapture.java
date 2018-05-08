/*
 * Copyright © 2016, Saleforce.com, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ehoffman.advised.logback.internal;

import static ch.qos.logback.classic.Level.ALL;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ch.qos.logback.classic.Level;
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
    if (INSTANCE.get() != null) {
      throw new IllegalStateException("already started");
    }
    INSTANCE.set(new LogbackCapture(null, null, null));
  }

  /**
   * Start capturing.
   * 
   * @param loggerName
   *          if null, defaults to the root logger
   * @param level
   *          if null, defaults to all levels
   * @param layoutPattern
   *          if null, defaults to "[%p] %m%n"
   */
  public static void start(final String loggerName, final Level level, final String layoutPattern) {
    if (INSTANCE.get() != null) {
      throw new IllegalStateException("already started");
    }
    INSTANCE.set(new LogbackCapture(loggerName, level, layoutPattern));
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
    final Logger logger = getContext().getLogger(name);
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
    LoggerContext context = null;
    int retryCount = 0;
    while (context == null && retryCount < 10) {
      try {
        context = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
        retryCount++;
        if (context == null) {
          Thread.sleep(500);
        }
      } catch (NullPointerException ex) {
        if (retryCount == 10) {
          throw ex;
        }
      } catch (InterruptedException iex) {
        throw new RuntimeException(iex);
      }
    }
    return context;
  }
}
