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

import com.google.cloud.tools.appengine.operations.CloudSdk;
import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.maven.it.verifier.TailingVerifier;
import java.io.IOException;
import java.util.Arrays;
import org.apache.maven.it.VerificationException;
import org.junit.BeforeClass;

public abstract class AbstractMojoIntegrationTest {

  private static boolean doneInstallPlugin = false;

  @BeforeClass
  public static void installPlugin() throws VerificationException {
    // install the plugin under test
    if (!doneInstallPlugin) {
      TailingVerifier verifier = new TailingVerifier("installPlugin", ".");
      verifier.addCliOption("-DskipTests");
      verifier.executeGoal("install");
      doneInstallPlugin = true;
    }
  }

  protected void deleteService(String service)
      throws CloudSdkNotFoundException, IOException, ProcessHandlerException {
    CloudSdk cloudSdk = new CloudSdk.Builder().build();
    Gcloud.builder(cloudSdk)
        .build()
        .runCommand(Arrays.asList("app", "services", "delete", service));
  }
}
