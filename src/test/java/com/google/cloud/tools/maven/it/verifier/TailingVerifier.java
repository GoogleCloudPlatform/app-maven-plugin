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

package com.google.cloud.tools.maven.it.verifier;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

public class TailingVerifier extends Verifier {

  public static final int TAIL_DELAY_MILLIS = 1000;

  private String testName;

  public TailingVerifier(String testName, String basedir) throws VerificationException {
    super(basedir, true);
    this.testName = testName;
    setLogFileName(testName + ".txt");
    setAutoclean(false);

    startTailingLog();
  }

  private void startTailingLog() {
    TailerListener listener =
        new TailerListenerAdapter() {
          @Override
          public void handle(String line) {
            System.out.println(testName + ": " + line);
          }
        };

    // Tail the log
    File file = new File(getBasedir() + File.separator + getLogFileName());
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Tailer tailer = new Tailer(file, listener, TAIL_DELAY_MILLIS);
    Thread thread = new Thread(tailer);
    thread.setDaemon(true);
    thread.start();
  }
}
