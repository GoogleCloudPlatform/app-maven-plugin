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


import com.google.cloud.tools.maven.it.verifier.FlexibleVerifier;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.IOException;

public class StageMojoIntegrationTest extends AbstractMojoIntegrationTest {

  @Test
  public void testStageStandard() throws IOException, VerificationException {

    String projectDir = ResourceExtractor
        .simpleExtractResources(getClass(), "/projects/standard-project")
        .getAbsolutePath();

    Verifier verifier = new StandardVerifier("testStageStandard");

    // execute with staging directory not present
    verifier.executeGoal("appengine:stage");

    // verify
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Detected App Engine standard environment application");
    verifier.verifyTextInLog("GCLOUD: ");
    verifier.assertFilePresent("target/appengine-staging");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF/web.xml");
    verifier.assertFilePresent("target/appengine-staging/app.yaml");
    verifier.assertFileMatches(projectDir + "/target/appengine-staging/app.yaml",
        "(?s).*service: 'standard-project'.*");

    // repeat with staging directory present
    verifier.setLogFileName("testStageStandard_repeat.txt");
    verifier.setAutoclean(false);
    verifier.executeGoal("appengine:stage");
    verifier.verifyTextInLog("Deleting the staging directory");
  }


  @Test
  public void testStageFlexible() throws IOException, VerificationException {

    Verifier verifier = new FlexibleVerifier("testStageFlexible");

    // execute stage
    verifier.executeGoal("appengine:stage");

    // verify
    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog("Detected App Engine flexible environment application");
    verifier.assertFilePresent("target/appengine-staging");
    verifier.assertFilePresent("target/appengine-staging/flexible-project-1.0-SNAPSHOT.war");
    verifier.assertFilePresent("target/appengine-staging/app.yaml");
  }

}
