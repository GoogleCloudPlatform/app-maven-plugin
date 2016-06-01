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
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    CloudSdk.Builder cloudSdkStageBuilderMock = mock(CloudSdk.Builder.class, RETURNS_SELF);
    CloudSdk.Builder cloudSdkDeployBuilderMock = mock(CloudSdk.Builder.class, RETURNS_SELF);
    Factory factoryMock = mock(Factory.class);

    // create mojo
    DeployMojo deployMojo = new DeployMojo();
    deployMojo.factory = factoryMock;
    deployMojo.pluginDescriptor = pluginDescriptorMock;
    deployMojo.deployables = new ArrayList<>();
    deployMojo.stagingDirectory = tempFolder.newFolder("staging");
    deployMojo.sourceDirectory = tempFolder.newFolder("source");

    // wire up
    when(factoryMock.standardStaging(cloudSdkMock)).thenReturn(standardStagingMock);
    when(factoryMock.deployment(cloudSdkMock)).thenReturn(deploymentMock);
    when(factoryMock.logger(deployMojo)).thenReturn(logMock);
    when(factoryMock.cloudSdkBuilder())
        .thenReturn(cloudSdkStageBuilderMock, cloudSdkDeployBuilderMock);
    when(cloudSdkStageBuilderMock.build()).thenReturn(cloudSdkMock);
    when(cloudSdkDeployBuilderMock.build()).thenReturn(cloudSdkMock);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    verify(standardStagingMock).stageStandard(deployMojo);
    verify(deploymentMock).deploy(deployMojo);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojo, cloudSdkStageBuilderMock);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojo, cloudSdkDeployBuilderMock);
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
    CloudSdk.Builder cloudSdkDeployBuilderMock = mock(CloudSdk.Builder.class, RETURNS_SELF);
    Factory factoryMock = mock(Factory.class);

    // create mojo
    DeployMojo deployMojo = new DeployMojo();
    deployMojo.factory = factoryMock;
    deployMojo.pluginDescriptor = pluginDescriptorMock;
    deployMojo.deployables = new ArrayList<>();
    deployMojo.stagingDirectory = tempFolder.newFolder("staging");
    deployMojo.sourceDirectory = tempFolder.newFolder("source");

    // wire up
    when(factoryMock.deployment(cloudSdkMock)).thenReturn(deploymentMock);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.cloudSdkBuilder()).thenReturn(cloudSdkDeployBuilderMock);
    when(factoryMock.logger(deployMojo)).thenReturn(logMock);
    when(cloudSdkDeployBuilderMock.build()).thenReturn(cloudSdkMock);

    // invoke
    deployMojo.execute();

    // verify
    assertEquals(1, deployMojo.deployables.size());
    verify(flexibleStagingMock).stageFlexible(deployMojo);
    CloudSdkMojoTest.verifyCloudSdkCommon(deployMojo, cloudSdkDeployBuilderMock);
  }
}
