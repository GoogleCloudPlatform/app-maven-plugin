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

package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
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
public class DeployMojoTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkAppEngineFactory factoryMock;

  @Mock private MavenProject project;

  @Mock private AppEngineFlexibleStaging flexibleStagingMock;

  @Mock private AppEngineStandardStaging standardStagingMock;

  @Mock private AppEngineDeployment deploymentMock;

  @Mock private Log log;

  @InjectMocks private DeployMojo deployMojo;

  @Before
  public void wireUpDeployMojo() throws IOException {
    MockitoAnnotations.initMocks(this);
    deployMojo.deployables = new ArrayList<>();
    deployMojo.stagingDirectory = tempFolder.newFolder("staging");
    deployMojo.sourceDirectory = tempFolder.newFolder("source");
    when(project.getProperties()).thenReturn(new Properties());
    when(project.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
  }

  @Test
  @Parameters({"jar", "war"})
  public void testDeployStandard(String packaging)
      throws IOException, MojoFailureException, MojoExecutionException {

    // wire up
    when(project.getPackaging()).thenReturn(packaging);
    when(factoryMock.standardStaging()).thenReturn(standardStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();
    Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    verify(standardStagingMock).stageStandard(deployMojo);
    verify(deploymentMock).deploy(deployMojo);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testDeployFlexible(String packaging) throws Exception {

    // wire up
    when(project.getPackaging()).thenReturn(packaging);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    assertEquals(deployMojo.stagingDirectory, deployMojo.deployables.get(0));
    verify(flexibleStagingMock).stageFlexible(deployMojo);
    verify(deploymentMock).deploy(deployMojo);
  }

  @Test
  @Parameters({"jar", "war"})
  public void testDeploySpecifiedAppYaml(String packaging) throws Exception {
    File appYaml = new File("myApp.yaml");
    deployMojo.deployables = Arrays.asList(appYaml);

    // wire up
    when(project.getPackaging()).thenReturn(packaging);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    assertEquals(appYaml, deployMojo.deployables.get(0));
    verify(flexibleStagingMock).stageFlexible(deployMojo);
    verify(deploymentMock).deploy(deployMojo);
  }
}
