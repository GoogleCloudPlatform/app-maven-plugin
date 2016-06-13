/*
 * Copyright (C) 2016 Google Inc.
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

package com.google.cloud.tools.maven.it;

import static org.junit.Assert.assertEquals;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

// TODO: make the test succeed more reliably on AppVeyor
@Ignore
public class RunMojoIntegrationTest extends AbstractMojoIntegrationTest {

  @Test
  public void testRunStandard() throws IOException, VerificationException, InterruptedException {

    String projectDir = ResourceExtractor
        .simpleExtractResources(getClass(), "/projects/standard-project")
        .getAbsolutePath();

    final Verifier verifier = new Verifier(projectDir);
    verifier.setLogFileName("testRunStandard.txt");

    // execute
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          verifier.executeGoal("appengine:run");
        } catch (VerificationException e) {
          throw new RuntimeException(e);
        }
      }
    };
    thread.start();

    try {
      // wait up to 60 seconds for the server to start (retry every 200ms)
      String urlContent = getUrlContentWithRetries("http://localhost:8080/", 60000, 200);

      // verify
      assertEquals("Hello from the App Engine Standard project.", urlContent);
      verifier.verifyErrorFreeLog();
      verifier.verifyTextInLog("Dev App Server is now running");
    } finally {
      // stop server
      verifier.setLogFileName("testRunStandard_stop.txt");
      verifier.setAutoclean(false);
      verifier.executeGoal("appengine:stop");
    }
  }


}
