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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.junit.Test;

import java.io.File;

public class CloudSdkMojoTest {

  public static final String ARTIFACT_ID = "gcp-app-maven-plugin";
  public static final String ARTIFACT_VERSION = "0.1.0";

  @Test
  public void testConfigureCloudSdkBuilder() throws MojoFailureException, MojoExecutionException {

    // create mocks
    PluginDescriptor pluginDescriptorMock = createPluginDescriptorMock();

    // create spies
    CloudSdkMojo mojoSpy = spy(CloudSdkMojo.class);
    CloudSdk.Builder cloudSdkBuilderSpy = spy(CloudSdk.Builder.class);

    // wire up
    mojoSpy.pluginDescriptor = pluginDescriptorMock;
    mojoSpy.cloudSdkPath = new File("google-cloud-sdk");

    // invoke
    mojoSpy.configureCloudSdkBuilder(cloudSdkBuilderSpy).build();

    // verify
    verifyCloudSdkCommon(mojoSpy, cloudSdkBuilderSpy);
  }

  public static PluginDescriptor createPluginDescriptorMock() {
    PluginDescriptor pluginDescriptorMock = mock(PluginDescriptor.class);
    when(pluginDescriptorMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(pluginDescriptorMock.getVersion()).thenReturn(ARTIFACT_VERSION);
    return pluginDescriptorMock;
  }

  public static void verifyCloudSdkCommon(CloudSdkMojo mojo, CloudSdk.Builder cloudSdkBuilderSpy) {
    assertNotNull(mojo.gcloudOutputListener);
    verify(cloudSdkBuilderSpy).sdkPath(mojo.cloudSdkPath);
    verify(cloudSdkBuilderSpy).addStdOutLineListener(mojo.gcloudOutputListener);
    verify(cloudSdkBuilderSpy).addStdErrLineListener(mojo.gcloudOutputListener);
    verify(cloudSdkBuilderSpy).appCommandMetricsEnvironment("gcp-app-maven-plugin");
    verify(cloudSdkBuilderSpy).appCommandMetricsEnvironmentVersion("0.1.0");
    verify(cloudSdkBuilderSpy).build();
  }

  public void testCreateCloudSdkBuilder() {
    CloudSdkMojo mojoSpy = spy(CloudSdkMojo.class);
  }
}