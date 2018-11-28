/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

import com.google.cloud.tools.maven.genrepoinfo.GenRepoInfoFileMojo;
import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import java.io.IOException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

/** {@link GenRepoInfoFileMojo} integration tests. */
public class GenRepoInfoFileMojoIntegrationTest extends AbstractMojoIntegrationTest {
  @Test
  public void testGenerate() throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier("testGenRepoInfoFile");

    verifier.executeGoal("appengine:genRepoInfoFile");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF/classes/source-context.json");
  }

  /** Ensures that this goal is ran with the package phase. */
  @Test
  public void testGenerateCallingPackage() throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier("testGenRepoInfoFile");

    verifier.executeGoal("package");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF/classes/source-context.json");
  }
}
