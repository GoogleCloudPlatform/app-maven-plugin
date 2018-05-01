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

import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.InvalidJavaSdkException;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.maven.it.verifier.FlexibleVerifier;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import java.io.IOException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class DeployMojoIntegrationTest extends AbstractMojoIntegrationTest {

  @Test
  public void testDeployStandard()
      throws IOException, VerificationException, ProcessRunnerException, CloudSdkNotFoundException,
          CloudSdkOutOfDateException, CloudSdkVersionFileException, InvalidJavaSdkException {

    Verifier verifier = new StandardVerifier("testDeployStandard");

    // execute with staging directory not present
    verifier.executeGoal("package");
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
      throws IOException, VerificationException, ProcessRunnerException, CloudSdkNotFoundException,
          CloudSdkOutOfDateException, CloudSdkVersionFileException, InvalidJavaSdkException {

    Verifier verifier = new FlexibleVerifier("testDeployFlexible");

    // execute with staging directory not present
    verifier.executeGoal("package");
    verifier.executeGoal("appengine:deploy");

    // verify
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Detected App Engine flexible environment application");
    verifier.verifyTextInLog("GCLOUD: Deployed service");

    // verify debugger required file generation
    verifier.assertFilePresent(
        "target/flexible-project-1.0-SNAPSHOT/WEB-INF/classes/" + "source-context.json");

    // cleanup
    deleteService("flexible-project");
  }
}
