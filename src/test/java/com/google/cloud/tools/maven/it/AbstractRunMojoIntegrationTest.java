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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AbstractRunMojoIntegrationTest extends AbstractMojoIntegrationTest {

  private int serverPort;
  private int adminPort;

  @Before
  public void initPorts() throws IOException {
    serverPort = SocketUtil.findPort();
    adminPort = SocketUtil.findPort();
  }

  @Test
  @Parameters
  public void testRun(String[] profiles, String expectedModuleName)
      throws IOException, VerificationException, InterruptedException, ExecutionException {

    String name = "testRun" + Arrays.toString(profiles);
    Verifier verifier = createVerifier(name);
    Arrays.stream(profiles)
        .filter(profile -> !profile.isEmpty())
        .map("-P"::concat)
        .forEach(verifier::addCliOption);

    ExecutorService executor = Executors.newSingleThreadExecutor(); // sequential execution
    Future<String> urlContent =
        // wait up to 8 minutes for the server to start (retry every second)
        // it may take a long time when installing Cloud SDK components for the first time
        executor.submit(() -> UrlUtils.getUrlContentWithRetries(getServerUrl(), 480000, 1000));
    Future<Boolean> isUrlDown =
        executor.submit(
            () -> {
              // stop server
              createVerifier(name + "_stop").executeGoal("appengine:stop");
              // wait up to 5 seconds for the server to stop
              return UrlUtils.isUrlDownWithRetries(getServerUrl(), 5000, 100);
            });
    executor.shutdown();

    // execute
    verifier.executeGoals(Arrays.asList("package", "appengine:run"));

    assertThat(urlContent.get(), containsString("Hello from the App Engine Standard project."));
    assertThat(urlContent.get(), containsString("TEST_VAR=testVariableValue"));
    assertTrue(isUrlDown.get());
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Dev App Server is now running");
    verifier.verifyTextInLog("Module instance " + expectedModuleName + " is running");
  }

  /** Provides parameters for {@link #testRun(String[], String)}. */
  @SuppressWarnings("unused")
  private Object[] parametersForTestRun() {
    List<Object[]> result = new ArrayList<>();
    result.add(new Object[] {new String[0], "standard-project"});
    result.add(
        new Object[] {new String[] {"base-it-profile", "services"}, "standard-project-services"});
    return result.toArray(new Object[0]);
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
