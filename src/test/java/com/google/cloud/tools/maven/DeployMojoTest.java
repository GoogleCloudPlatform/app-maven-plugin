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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DeployMojoTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testDeployStandard()
      throws IOException, MojoFailureException, MojoExecutionException {

    // create mocks
    CloudSdkAppEngineStandardStaging standardStagingMock = mock(
        CloudSdkAppEngineStandardStaging.class);
    CloudSdk cloudSdkMock = mock(CloudSdk.class);
    CloudSdkAppEngineDeployment deploymentMock = mock(CloudSdkAppEngineDeployment.class);
    PluginDescriptor pluginDescriptorMock = CloudSdkMojoTest.createPluginDescriptorMock();
    Log logMock = mock(Log.class);

    // create spies
    DeployMojo deployMojoSpy = spy(DeployMojo.class);
    CloudSdk.Builder cloudSdkStageBuilderSpy = spy(CloudSdk.Builder.class);
    CloudSdk.Builder cloudSdkDeployBuilderSpy = spy(CloudSdk.Builder.class);

    // wire up
    doReturn(standardStagingMock).when(deployMojoSpy).getStandardStaging(cloudSdkMock);
    doReturn(deploymentMock).when(deployMojoSpy).getDeployment(cloudSdkMock);
    doReturn(cloudSdkMock).when(cloudSdkStageBuilderSpy).build();
    doReturn(cloudSdkMock).when(cloudSdkDeployBuilderSpy).build();
    doReturn(cloudSdkStageBuilderSpy).doReturn(cloudSdkDeployBuilderSpy)
        .when(deployMojoSpy).createCloudSdkBuilder();
    doReturn(logMock).when(deployMojoSpy).getLog();
    deployMojoSpy.pluginDescriptor = pluginDescriptorMock;
    deployMojoSpy.deployables = new ArrayList<>();
    deployMojoSpy.stagingDirectory = tempFolder.newFolder("staging");
    deployMojoSpy.sourceDirectory = tempFolder.newFolder("source");

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();

    // invoke
    deployMojoSpy.execute();

    // verify
    assertEquals(1, deployMojoSpy.deployables.size());
    //   verify(deployMojoSpy, times(2)).createCloudSdk();
    verify(standardStagingMock).stageStandard(deployMojoSpy);
    verify(deploymentMock).deploy(deployMojoSpy);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojoSpy, cloudSdkStageBuilderSpy);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojoSpy, cloudSdkDeployBuilderSpy);
  }

  @Test
  public void testDeployFlexible() throws Exception {

    // create mocks
    CloudSdkAppEngineFlexibleStaging flexibleStagingMock = mock(
        CloudSdkAppEngineFlexibleStaging.class);
    PluginDescriptor pluginDescriptorMock = CloudSdkMojoTest.createPluginDescriptorMock();
    CloudSdkAppEngineDeployment deploymentMock = mock(CloudSdkAppEngineDeployment.class);
    CloudSdk cloudSdkMock = mock(CloudSdk.class);
    Log logMock = mock(Log.class);

    // create spies
    DeployMojo deployMojoSpy = spy(DeployMojo.class);
    CloudSdk.Builder cloudSdkDeployBuilderSpy = spy(CloudSdk.Builder.class);

    // wire up
    doReturn(deploymentMock).when(deployMojoSpy).getDeployment(cloudSdkMock);
    doReturn(flexibleStagingMock).when(deployMojoSpy).getFlexibleStaging();
    doReturn(cloudSdkMock).when(cloudSdkDeployBuilderSpy).build();
    doReturn(cloudSdkDeployBuilderSpy).doReturn(cloudSdkDeployBuilderSpy)
        .when(deployMojoSpy).createCloudSdkBuilder();
    doReturn(logMock).when(deployMojoSpy).getLog();
    deployMojoSpy.pluginDescriptor = pluginDescriptorMock;
    deployMojoSpy.deployables = new ArrayList<>();
    deployMojoSpy.stagingDirectory = tempFolder.newFolder("staging");
    deployMojoSpy.sourceDirectory = tempFolder.newFolder("source");

    // invoke
    deployMojoSpy.execute();

    // verify
    assertEquals(1, deployMojoSpy.deployables.size());
    verify(flexibleStagingMock).stageFlexible(deployMojoSpy);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojoSpy, cloudSdkDeployBuilderSpy);
  }
}
