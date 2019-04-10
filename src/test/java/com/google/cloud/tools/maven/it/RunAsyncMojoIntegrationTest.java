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

import com.google.cloud.tools.maven.it.util.UrlUtils;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import com.google.cloud.tools.maven.util.SocketUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

public class RunAsyncMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private static final String DEV_APP_SERVER_STARTED = "INFO:oejs.Server:main: Started";
  private int serverPort;

  @Before
  public void initPorts() throws IOException {
    serverPort = SocketUtil.findPort();
  }

  @Test
  public void testRunAsync() throws IOException, VerificationException, InterruptedException {
    String testName = "testRunAsync";
    try {
      Verifier verifier = createVerifier(testName);
      verifier.setSystemProperty("app.devserver.startSuccessTimeout", "60");
      verifier.executeGoals(Arrays.asList("package", "appengine:start"));

      String urlContent = UrlUtils.getUrlContentWithRetries(getServerUrl(), 60000, 1000);
      assertThat(urlContent, containsString("Hello from the App Engine Standard project."));
      assertThat(urlContent, containsString("TEST_VAR=testVariableValue"));

      Path expectedLog =
          Paths.get(
              "target",
              "test-classes",
              "projects",
              "standard-project",
              "target",
              "dev-appserver-out",
              "dev_appserver.out");
      assertTrue(Files.exists(expectedLog));
      String devAppServerOutput =
          new String(Files.readAllBytes(expectedLog), StandardCharsets.UTF_8);
      assertTrue(devAppServerOutput.contains(DEV_APP_SERVER_STARTED));

      verifier.verifyErrorFreeLog();
      verifier.verifyTextInLog(DEV_APP_SERVER_STARTED);
    } finally {
      Verifier stopVerifier = createVerifier(testName + "_stop");
      stopVerifier.executeGoal("appengine:stop");
      // wait up to 5 seconds for the server to stop
      assertTrue(UrlUtils.isUrlDownWithRetries(getServerUrl(), 5000, 100));
    }
  }

  private Verifier createVerifier(String name) throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier(name);
    verifier.setSystemProperty("app.devserver.port", Integer.toString(serverPort));
    return verifier;
  }

  private String getServerUrl() {
    return "http://localhost:" + serverPort;
  }
}
