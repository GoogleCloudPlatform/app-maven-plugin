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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.IOException;

public class StopMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private final String SERVER_URL = "http://localhost:8080";

  @Test
  public void testStopStandard()
      throws IOException, VerificationException, InterruptedException {

    String projectDir = ResourceExtractor
        .simpleExtractResources(getClass(), "/projects/standard-project")
        .getAbsolutePath();

    Verifier verifier = new Verifier(projectDir);

    // start dev app server
    verifier.setLogFileName("testStopStandard_start.txt");
    verifier.executeGoal("appengine:start");

    // verify dev app server is up
    verifier.verifyErrorFreeLog();
    assertNotNull(getUrlContentWithRetries(SERVER_URL, 5000, 100));

    // stop dev app server
    verifier.setLogFileName("testStopStandard.txt");
    verifier.setAutoclean(false);
    verifier.executeGoal("appengine:stop");

    // verify dev app server is down
    verifier.verifyErrorFreeLog();
    // wait up to 5 seconds for the server to stop
    assertTrue(isUrlDownWithRetries(SERVER_URL, 5000, 100));

  }
}
