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

package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class DeployMojoTest extends CloudSdkMojoTest {

  @InjectMocks
  private DeployMojo deployMojo;

  @Before
  public void wireUpDeployMojo() throws IOException {
    deployMojo.deployables = new ArrayList<>();
    deployMojo.stagingDirectory = tempFolder.newFolder("staging");
    deployMojo.sourceDirectory = tempFolder.newFolder("source");
  }

  @Test
  public void testDeployStandard()
      throws IOException, MojoFailureException, MojoExecutionException {

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    verify(standardStagingMock).stageStandard(deployMojo);
    verify(deploymentMock).deploy(deployMojo);
    verifyCloudSdkCommon(deployMojo, cloudSdkBuilderMock);
    verifyCloudSdkCommon(deployMojo, cloudSdkBuilderMock2);
  }

  @Test
  public void testDeployFlexible() throws Exception {

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    verify(flexibleStagingMock).stageFlexible(deployMojo);
    verifyCloudSdkCommon(deployMojo, cloudSdkBuilderMock);
  }
}
