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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;
import com.google.cloud.tools.maven.it.util.UrlUtils;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import com.google.cloud.tools.maven.util.SocketUtil;
import java.io.IOException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

public class RunAsyncMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private int serverPort;
  private int adminPort;

  @Before
  public void initPorts() throws IOException {
    serverPort = SocketUtil.findPort();
    adminPort = SocketUtil.findPort();
  }

  @Test
  public void testRunAsyncStandardV1()
      throws IOException, VerificationException, InterruptedException {
    test("testRunAsyncV1", SupportedDevServerVersion.V1);
  }

  @Test
  public void testRunAsyncStandardV2Alpha()
      throws IOException, VerificationException, InterruptedException {
    test("testRunAsyncV2Alpha", SupportedDevServerVersion.V2ALPHA);
  }

  private void test(String name, SupportedDevServerVersion version)
      throws IOException, VerificationException, InterruptedException {
    try {
      Verifier verifier = createVerifier(name, version);
      verifier.setSystemProperty("app.devserver.startSuccessTimeout", "60");
      verifier.executeGoal("appengine:start");

      String urlContent = UrlUtils.getUrlContentWithRetries(getServerUrl(), 60000, 1000);
      assertThat(urlContent, containsString("Hello from the App Engine Standard project."));
      assertThat(urlContent, containsString("TEST_VAR=testVariableValue"));
      verifier.verifyErrorFreeLog();
      verifier.verifyTextInLog("Dev App Server is now running");
    } finally {
      Verifier stopVerifier = createVerifier(name + "_stop", version);
      stopVerifier.executeGoal("appengine:stop");
      // wait up to 5 seconds for the server to stop
      assertTrue(UrlUtils.isUrlDownWithRetries(getServerUrl(), 5000, 100));
    }
  }

  private Verifier createVerifier(String name, SupportedDevServerVersion version)
      throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier(name);
    verifier.setSystemProperty("app.devserver.port", Integer.toString(serverPort));
    if (version == SupportedDevServerVersion.V2ALPHA) {
      verifier.setSystemProperty("app.devserver.adminPort", Integer.toString(adminPort));
      verifier.setSystemProperty("app.devserver.version", "2-alpha");
    }
    return verifier;
  }

  private String getServerUrl() {
    return "http://localhost:" + serverPort;
  }
}
