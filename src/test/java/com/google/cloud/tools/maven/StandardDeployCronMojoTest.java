/*
 * Copyright (C) 2017 Google Inc.
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

package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.maven.util.SingleYamlDeployTestHelper;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class StandardDeployCronMojoTest {

  private DeployCronMojo mojo = new DeployCronMojo();

  private TemporaryFolder tempFolder = new TemporaryFolder();

  private SingleYamlDeployTestHelper<DeployCronMojo> testFixture =
      SingleYamlDeployTestHelper.forStandard(mojo, tempFolder);

  @Rule
  public TestRule testRule = RuleChain.outerRule(tempFolder).around(testFixture);

  @Test
  public void testDeployStandard()
      throws IOException, MojoFailureException, MojoExecutionException {
    mojo.execute();

    assertEquals(Paths.get(tempFolder.getRoot().getAbsolutePath(),
        "staging", "WEB-INF", "appengine-generated").toString(),
        mojo.appEngineDirectory.getAbsolutePath());
    verify(testFixture.getDeploymentMock()).deployCron(mojo);
  }
}
