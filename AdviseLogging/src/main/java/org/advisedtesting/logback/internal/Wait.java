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
package org.advisedtesting.logback.internal;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class Wait<T> {

  private Supplier<T> supplier = null;
  private int retries = 1;
  
  public Wait<T> on(Supplier<T> supplier)  {
    this.supplier = supplier;
    return this;
  }
  
  public Wait<T> trying(int times) {
    this.retries = times;
    return this;
  }
  
  /**
   * Waits on the prior specified supplier to be able to return a non null value, or throw an {@link InterruptedException}.
   * @return the output if available before the number of retries are up, 
   * @throws RuntimeException if the last retry resulted in an exception.
   * @throws CompletionException if the process is interrupted.
   */
  public T toComplete() {
    T output = null;
    int retryCount = 0;
    while (output == null && retryCount < retries) {
      try {
        output = supplier.get(); 
        retryCount++;
        if (output == null) {
          Thread.sleep(500);
        }
      } catch (RuntimeException ex) {
        if (retryCount == retries - 1) {
          throw ex;
        }
        retryCount++;
      } catch (InterruptedException iex) {
        throw new CompletionException(iex);
      }
    }
    return output;
  }
  
}
