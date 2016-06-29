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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.IOException;


public class RunAsyncMojoIntegrationTest extends AbstractMojoIntegrationTest {
  private final String SERVER_URL = "http://localhost:8080";

  @Test
  public void testRunAsyncStandard()
      throws IOException, VerificationException, InterruptedException {

    Verifier verifier = createStandardVerifier("testRunAsync");

    try {
      // execute
      verifier.executeGoal("appengine:start");

      // verify
      assertEquals("Hello from the App Engine Standard project.",
          getUrlContentWithRetries(SERVER_URL, 5000, 1000));
      verifier.verifyErrorFreeLog();
      verifier.verifyTextInLog("Dev App Server is now running");
    } finally {
      // stop server
      Verifier stopVerifier = createStandardVerifier("testRunAsyncStandard_stop");
      stopVerifier.executeGoal("appengine:stop");
      // wait up to 5 seconds for the server to stop
      assertTrue(isUrlDownWithRetries(SERVER_URL, 5000, 100));
    }
  }
}
