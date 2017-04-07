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
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;
import com.google.cloud.tools.maven.it.util.UrlUtils;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

import java.io.IOException;

public class RunMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private static final String ADMIN_PORT = "28082";
  private static final String SERVER_PORT = "28081";
  private static final String SERVER_URL = "http://localhost:" + SERVER_PORT;

  @Test
  public void testRunStandardV1() throws IOException, VerificationException, InterruptedException {
    test("testRunV1", SupportedDevServerVersion.V1);
  }

  @Test
  public void testRunStandardV2Alpha()
      throws IOException, VerificationException, InterruptedException {
    test("testRunV2Alpha", SupportedDevServerVersion.V2ALPHA);
  }

  private void test(final String name, final SupportedDevServerVersion version)
      throws IOException, VerificationException, InterruptedException {
    final Verifier verifier = createVerifier(name, version);
    final StringBuilder urlContent = new StringBuilder();

    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          // wait up to 60 seconds for the server to start (retry every second)
          urlContent.append(UrlUtils.getUrlContentWithRetries(SERVER_URL, 60000, 1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          // stop server
          try {
            Verifier stopVerifier = createVerifier(name + "_stop", version);
            stopVerifier.executeGoal("appengine:stop");
            // wait up to 5 seconds for the server to stop
            assertTrue(UrlUtils.isUrlDownWithRetries(SERVER_URL, 5000, 100));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    };
    thread.setDaemon(true);
    thread.start();

    // execute
    verifier.setSystemProperty("app.devserver.port", SERVER_PORT);
    verifier.setSystemProperty("app.devserver.adminPort", ADMIN_PORT);
    verifier.executeGoal("appengine:run");

    thread.join();

    // verify
    assertEquals("Hello from the App Engine Standard project.", urlContent.toString());
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Dev App Server is now running");
  }

  private Verifier createVerifier(String name, SupportedDevServerVersion version)
      throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier(name);
    verifier.setSystemProperty("app.devserver.port", SERVER_PORT);
    if (version == SupportedDevServerVersion.V2ALPHA) {
      verifier.setSystemProperty("app.devserver.adminPort", ADMIN_PORT);
      verifier.setSystemProperty("app.devserver.version", "2-alpha");
    }
    return verifier;
  }
}
