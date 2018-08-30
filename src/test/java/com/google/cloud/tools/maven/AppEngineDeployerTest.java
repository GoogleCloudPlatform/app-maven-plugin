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

package com.google.cloud.tools.maven;

import java.io.IOException;
import junitparams.JUnitParamsRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class AppEngineDeployerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private DeployMojo deployMojo;
  @Mock private MavenProject mavenProject;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    Mockito.when(deployMojo.getSourceDirectory()).thenReturn(tempFolder.getRoot());
    Mockito.when(deployMojo.getMavenProject()).thenReturn(mavenProject);
    Mockito.when(mavenProject.getBasedir()).thenReturn(tempFolder.getRoot());
    Mockito.when(deployMojo.getProjectId()).thenReturn("someproject");
    Mockito.when(deployMojo.getVersion()).thenReturn("someversion");
    Mockito.when(deployMojo.getStagingDirectory()).thenReturn(tempFolder.getRoot());
  }

  @Test
  public void testNewDeployer_standard() throws MojoExecutionException {
    Mockito.when(deployMojo.isStandardStaging()).thenReturn(true);
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot());

    AppEngineDeployer deployer = AppEngineDeployer.Factory.newDeployer(deployMojo);
    Assert.assertTrue(deployer.getClass().equals(AppEngineStandardDeployer.class));
  }

  @Test
  public void testNewDeployer_flexible() throws MojoExecutionException {
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot());

    AppEngineDeployer deployer = AppEngineDeployer.Factory.newDeployer(deployMojo);
    Assert.assertTrue(deployer.getClass().equals(AppEngineFlexibleDeployer.class));
  }

  @Test
  public void testNewDeployer_noArtifact() {
    try {
      AppEngineDeployer.Factory.newDeployer(deployMojo);
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(
          "\nCould not determine appengine environment, did you package your application?"
              + "\nRun 'mvn package appengine:deploy'",
          ex.getMessage());
    }
  }
}
