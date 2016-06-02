/*
 * Copyright (c) 2016 Google Inc. All Right Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.cloud.tools.maven;

import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessOutputLineListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

public abstract class CloudSdkMojoTest {

  public static final String ARTIFACT_ID = "gcp-app-maven-plugin";
  public static final String ARTIFACT_VERSION = "0.1.0";

  protected PluginDescriptor pluginDescriptorMock = createPluginDescriptorMock();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock(answer = RETURNS_SELF)
  protected CloudSdk.Builder cloudSdkBuilderMock;

  @Mock(answer = RETURNS_SELF)
  protected CloudSdk.Builder cloudSdkBuilderMock2;

  @Mock
  protected Log logMock;

  @Mock
  protected CloudSdk cloudSdkMock;

  @Mock
  protected AppEngineFactory factoryMock;

  @Mock
  protected CloudSdkAppEngineStandardStaging standardStagingMock;

  @Mock
  protected CloudSdkAppEngineFlexibleStaging flexibleStagingMock;

  @Mock
  protected CloudSdkAppEngineDeployment deploymentMock;

  @Mock
  protected AppEngineDevServer devServerMock;

  @Before
  public void wireUpMocks() {
    when(factoryMock.cloudSdkBuilder())
        .thenReturn(cloudSdkBuilderMock, cloudSdkBuilderMock2);
    when(factoryMock.standardStaging(cloudSdkMock)).thenReturn(standardStagingMock);
    when(factoryMock.deployment(cloudSdkMock)).thenReturn(deploymentMock);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.devServerStop()).thenReturn(devServerMock);
    when(factoryMock.devServer(any(CloudSdk.class))).thenReturn(devServerMock);
    when(cloudSdkBuilderMock.build()).thenReturn(cloudSdkMock);
    when(cloudSdkBuilderMock2.build()).thenReturn(cloudSdkMock);
  }

  private static PluginDescriptor createPluginDescriptorMock() {
    PluginDescriptor pluginDescriptorMock = mock(PluginDescriptor.class);
    when(pluginDescriptorMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(pluginDescriptorMock.getVersion()).thenReturn(ARTIFACT_VERSION);
    return pluginDescriptorMock;
  }

  protected static void verifyCloudSdkCommon(CloudSdkMojo mojo,
      CloudSdk.Builder cloudSdkBuilderMock) {
    verify(cloudSdkBuilderMock).sdkPath(mojo.cloudSdkPath);
    verify(cloudSdkBuilderMock).addStdOutLineListener(any(ProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).addStdErrLineListener(any(ProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironment(ARTIFACT_ID);
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironmentVersion(ARTIFACT_VERSION);
    verify(cloudSdkBuilderMock).build();
  }

}
