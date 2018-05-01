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

package com.google.cloud.tools.maven.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.maven.it.util.UrlUtils;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import com.google.cloud.tools.maven.util.SocketUtil;
import java.io.IOException;
import java.util.Arrays;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

public class StopMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private int serverPort;

  @Before
  public void initPorts() throws IOException {
    serverPort = SocketUtil.findPort();
  }

  @Test
  public void testStopStandard() throws IOException, VerificationException, InterruptedException {

    Verifier verifier = new StandardVerifier("testStopStandard_start");

    verifier.setSystemProperty("app.devserver.port", Integer.toString(serverPort));

    // start dev app server
    verifier.executeGoals(Arrays.asList("package", "appengine:start"));

    // verify dev app server is up
    verifier.verifyErrorFreeLog();
    assertNotNull(UrlUtils.getUrlContentWithRetries(getServerUrl(), 60000, 100));

    // stop dev app server
    verifier.setLogFileName("testStopStandard.txt");
    verifier.setAutoclean(false);
    verifier.executeGoal("appengine:stop");

    // verify dev app server is down
    verifier.verifyErrorFreeLog();
    // wait up to 5 seconds for the server to stop
    assertTrue(UrlUtils.isUrlDownWithRetries(getServerUrl(), 5000, 100));
  }

  private String getServerUrl() {
    return "http://localhost:" + serverPort;
  }
}
