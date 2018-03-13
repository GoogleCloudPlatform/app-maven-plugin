/*
 * Copyright 2016 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.maven.it.util;


import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

  /**
   * If the response code is 200, returns the content at the URL. Otherwise, returns null.
   */
  public static String getUrlContent(String url) {
    try {
      HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
      int responseCode = urlConnection.getResponseCode();
      if (responseCode == 200) {
        return CharStreams
            .toString(new InputStreamReader(urlConnection.getInputStream()));
      }
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * If the response code is 200, returns the content at the URL. Otherwise, retries multiple times
   * until successful or the timeout is reached.
   *
   * @param url The url to fetch.
   * @param timeoutMs The total number of milliseconds for which to keep retying.
   * @param retryDelayMs The number of milliseconds to wait between retries.
   * @return Content at the URL or null.
   */
  public static String getUrlContentWithRetries(String url, long timeoutMs, long retryDelayMs)
      throws InterruptedException {
    return getUrlContentWithRetries(url, timeoutMs, retryDelayMs, false);
  }

  /**
   * Returns true if the URL status code is not 200. Retries multiple times to see if the server
   * eventually goes down
   *
   * @param url The url to test.
   * @param timeoutMs The total number of milliseconds for which to keep retying.
   * @param retryDelayMs The number of milliseconds to wait between retries.
   * @return True if the server is down.
   */
  public static boolean isUrlDownWithRetries(String url, long timeoutMs, long retryDelayMs)
      throws InterruptedException {
    return getUrlContentWithRetries(url, timeoutMs, retryDelayMs, true) == null;
  }

  private static String getUrlContentWithRetries(String url, long timeoutMs, long retryDelayMs,
      boolean waitForFailure)
      throws InterruptedException {
    long totalWaitedMs = 0;
    String content = getUrlContent(url);

    while ((waitForFailure && content != null || !waitForFailure && content == null)
        && totalWaitedMs < timeoutMs) {
      long delay = Math.min(retryDelayMs, timeoutMs - totalWaitedMs);
      Thread.sleep(delay);
      totalWaitedMs += delay;
      content = getUrlContent(url);
    }

    return content;
  }
}
