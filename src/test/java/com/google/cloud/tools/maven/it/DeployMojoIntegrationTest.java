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

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.appengine.cloudsdk.process.NonZeroExceptionExitListener;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class DeployMojoIntegrationTest extends AbstractMojoIntegrationTest {

  @Test
  public void testDeployStandard()
      throws IOException, VerificationException, ProcessRunnerException {

    String projectDir = ResourceExtractor
        .simpleExtractResources(getClass(), "/projects/standard-project")
        .getAbsolutePath();

    Verifier verifier = new Verifier(projectDir);
    verifier.setLogFileName("testDeployStandard.txt");

    // execute with staging directory not present
    verifier.executeGoal("appengine:deploy");

    // verify
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Detected App Engine standard environment application");
    verifier.verifyTextInLog("GCLOUD: Deployed service");

    // cleanup
    deleteService("standard-project");
  }

  @Test
  public void testDeployFlexible()
      throws IOException, VerificationException, ProcessRunnerException {

    String projectDir = ResourceExtractor
        .simpleExtractResources(getClass(), "/projects/flexible-project")
        .getAbsolutePath();

    Verifier verifier = new Verifier(projectDir);
    verifier.setLogFileName("testDeployFlexible.txt");

    // execute with staging directory not present
    verifier.executeGoal("appengine:deploy");

    // verify
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Detected App Engine flexible environment application");
    verifier.verifyTextInLog("GCLOUD: Deployed service");

    // cleanup
    deleteService("flexible-project");
  }

  private void deleteService(String service) throws ProcessRunnerException {
    CloudSdk cloudSdk = new CloudSdk.Builder()
        .exitListener(new NonZeroExceptionExitListener())
        .build();
    cloudSdk.runAppCommand(Arrays.asList("services", "delete", service));
  }
}
