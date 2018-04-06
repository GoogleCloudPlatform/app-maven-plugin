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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class DeployAllMojoTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkAppEngineFactory factoryMock;
  @Mock private MavenProject project;
  @Mock private AppEngineStandardStaging standardStagingMock;
  @Mock private AppEngineFlexibleStaging flexibleStagingMock;
  @Mock private AppEngineDeployment deploymentMock;

  @InjectMocks private DeployAllMojo deployAllMojo;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    deployAllMojo.deployables = new ArrayList<>();
    deployAllMojo.stagingDirectory = tempFolder.newFolder("staging");
    deployAllMojo.sourceDirectory = tempFolder.newFolder("source");
    deployAllMojo.appEngineDirectory = tempFolder.newFolder("appengine");

    when(project.getProperties()).thenReturn(new Properties());
    when(project.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
    when(factoryMock.standardStaging()).thenReturn(standardStagingMock);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testExecute_standard(String packaging)
      throws IOException, MojoFailureException, MojoExecutionException, AppEngineException {
    when(project.getPackaging()).thenReturn(packaging);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();
    Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);

    // Make YAMLS
    File appYaml = tempFolder.newFile("staging/app.yaml");
    File cronYaml = tempFolder.newFile("staging/cron.yaml");
    File dispatchYaml = tempFolder.newFile("staging/dispatch.yaml");
    File dosYaml = tempFolder.newFile("staging/dos.yaml");
    File indexYaml = tempFolder.newFile("staging/index.yaml");
    File queueYaml = tempFolder.newFile("staging/queue.yaml");

    File invalidYaml = tempFolder.newFile("staging/invalid.yaml");

    deployAllMojo.execute();

    assertTrue(deployAllMojo.deployables.contains(appYaml));
    assertTrue(deployAllMojo.deployables.contains(cronYaml));
    assertTrue(deployAllMojo.deployables.contains(dispatchYaml));
    assertTrue(deployAllMojo.deployables.contains(dosYaml));
    assertTrue(deployAllMojo.deployables.contains(indexYaml));
    assertTrue(deployAllMojo.deployables.contains(queueYaml));
    assertFalse(deployAllMojo.deployables.contains(invalidYaml));
    verify(standardStagingMock).stageStandard(deployAllMojo);
    verify(deploymentMock).deploy(deployAllMojo);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testExecute_flexible(String packaging)
      throws IOException, MojoFailureException, MojoExecutionException, AppEngineException {
    when(project.getPackaging()).thenReturn(packaging);

    // Make YAMLS
    File appYaml = tempFolder.newFile("staging/app.yaml");
    File cronYaml = tempFolder.newFile("appengine/cron.yaml");
    File dispatchYaml = tempFolder.newFile("appengine/dispatch.yaml");
    File dosYaml = tempFolder.newFile("appengine/dos.yaml");
    File indexYaml = tempFolder.newFile("appengine/index.yaml");
    File queueYaml = tempFolder.newFile("appengine/queue.yaml");

    File invalidYamlStaging = tempFolder.newFile("staging/invalid.yaml");
    File invalidYamlAppEngine = tempFolder.newFile("appengine/invalid.yaml");

    deployAllMojo.execute();

    assertTrue(deployAllMojo.deployables.contains(appYaml));
    assertTrue(deployAllMojo.deployables.contains(cronYaml));
    assertTrue(deployAllMojo.deployables.contains(dispatchYaml));
    assertTrue(deployAllMojo.deployables.contains(dosYaml));
    assertTrue(deployAllMojo.deployables.contains(indexYaml));
    assertTrue(deployAllMojo.deployables.contains(queueYaml));
    assertFalse(deployAllMojo.deployables.contains(invalidYamlStaging));
    assertFalse(deployAllMojo.deployables.contains(invalidYamlAppEngine));
    verify(flexibleStagingMock).stageFlexible(deployAllMojo);
    verify(deploymentMock).deploy(deployAllMojo);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testExecute_validInDifferentDirStandard(String packaging)
      throws IOException, MojoFailureException, MojoExecutionException, AppEngineException {
    when(project.getPackaging()).thenReturn(packaging);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();
    Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);

    // Make YAMLS
    File appYaml = tempFolder.newFile("staging/app.yaml");
    File validInDifferentDirYaml = tempFolder.newFile("queue.yaml");

    deployAllMojo.execute();

    assertTrue(deployAllMojo.deployables.contains(appYaml));
    assertFalse(deployAllMojo.deployables.contains(validInDifferentDirYaml));
    verify(standardStagingMock).stageStandard(deployAllMojo);
    verify(deploymentMock).deploy(deployAllMojo);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testExecute_validInDifferentDirFlexible(String packaging)
      throws IOException, MojoFailureException, MojoExecutionException, AppEngineException {
    when(project.getPackaging()).thenReturn(packaging);

    // Make YAMLS
    File appYaml = tempFolder.newFile("staging/app.yaml");
    File cronYaml = tempFolder.newFile("appengine/cron.yaml");
    File validInDifferentDirYaml = tempFolder.newFile("queue.yaml");

    deployAllMojo.execute();

    assertTrue(deployAllMojo.deployables.contains(appYaml));
    assertTrue(deployAllMojo.deployables.contains(cronYaml));
    assertFalse(deployAllMojo.deployables.contains(validInDifferentDirYaml));
    verify(flexibleStagingMock).stageFlexible(deployAllMojo);
    verify(deploymentMock).deploy(deployAllMojo);
  }
}
