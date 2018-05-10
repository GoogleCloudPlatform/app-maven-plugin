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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class AppEngineFlexibleDeployerTest {
  private static final String PROJECT_BUILD = "project-build";
  private static final String VERSION_BUILD = "version-build";

  private static final String GCLOUD_CONFIG = "GCLOUD_CONFIG";
  private static final String APPENGINE_CONFIG = "APPENGINE_CONFIG";

  private static final String CONFIG_PROJECT_ERROR =
      "Deployment project must be defined or configured to read from system state\n"
          + "1. Set <project>my-project-name</project>\n"
          + "2. Set <project>"
          + GCLOUD_CONFIG
          + "</project> to use project from gcloud config.\n"
          + "3. Using <project>"
          + APPENGINE_CONFIG
          + "</project> is not allowed for flexible environment projects";

  private static final String CONFIG_VERSION_ERROR =
      "Deployment version must be defined or configured to read from system state\n"
          + "1. Set <version>my-version</version>\n"
          + "2. Set <version>"
          + GCLOUD_CONFIG
          + "</version> to use version from gcloud config.\n"
          + "3. Using <version>"
          + APPENGINE_CONFIG
          + "</version> is not allowed for flexible environment projects";

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private AbstractDeployMojo deployMojo;
  private AppEngineFlexibleDeployer appEngineStandardDeployer;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    deployMojo = new DeployMojo();
    deployMojo.sourceDirectory = tempFolder.newFolder("source");
    appEngineStandardDeployer = new AppEngineFlexibleDeployer(deployMojo);
  }

  @Test
  public void testUpdateGcloudProperties_fromBuildConfig() throws MojoExecutionException {
    deployMojo.version = VERSION_BUILD;
    deployMojo.project = PROJECT_BUILD;
    appEngineStandardDeployer.updateGcloudProperties();
    Assert.assertEquals(VERSION_BUILD, deployMojo.getVersion());
    Assert.assertEquals(PROJECT_BUILD, deployMojo.getProject());
  }

  @Test
  public void testUpdateGcloudProperties_fromGcloud() throws MojoExecutionException {
    deployMojo.version = GCLOUD_CONFIG;
    deployMojo.project = GCLOUD_CONFIG;
    appEngineStandardDeployer.updateGcloudProperties();
    Assert.assertEquals(null, deployMojo.getVersion());
    Assert.assertEquals(null, deployMojo.getProject());
  }

  @Test
  public void testUpdateGcloudProperties_projectFromAppengineWebXml() {
    deployMojo.version = VERSION_BUILD;
    deployMojo.project = APPENGINE_CONFIG;
    try {
      appEngineStandardDeployer.updateGcloudProperties();
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(CONFIG_PROJECT_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_versionFromAppengineWebXml() {
    deployMojo.version = APPENGINE_CONFIG;
    deployMojo.project = PROJECT_BUILD;
    try {
      appEngineStandardDeployer.updateGcloudProperties();
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(CONFIG_VERSION_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_noProjectSet() {
    deployMojo.version = VERSION_BUILD;
    deployMojo.project = null;
    try {
      appEngineStandardDeployer.updateGcloudProperties();
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(CONFIG_PROJECT_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_noVersionSet() {
    deployMojo.version = null;
    deployMojo.project = PROJECT_BUILD;
    try {
      appEngineStandardDeployer.updateGcloudProperties();
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(CONFIG_VERSION_ERROR, ex.getMessage());
    }
  }
}
