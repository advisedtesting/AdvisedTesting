/*
 * The MIT License
 * Copyright © 2016 AdvisedTesting
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
package com.github.advisedtesting.core.internal;

public class ExceptionEvaluator {

  /**
   * Attempts to find and exception of exceptionType, in the causedBy chain of throwable.
   * @param <T> an exception type represented by exceptionType parameter.
   * @param thowable the throwable to inspect.
   * @param exceptionType the class of exception we want (including subclass of)
   * @return null if not found, otherwise the found exception.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Exception> T convertExceptionIfPossible(Throwable thowable, Class<T> exceptionType) {
    if (exceptionType == null) {
      throw new IllegalArgumentException("Desired execption type cannot be null");
    }
    Throwable current = thowable;
    while (current != null) {
      if (exceptionType.isAssignableFrom(current.getClass())) {
        return (T) current;
      } else {
        current = current.getCause();
      }
    }
    return null;
  }
}
