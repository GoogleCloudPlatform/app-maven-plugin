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

import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.junit.Test;

public class RunMojoTest {

  @Test
  public void testRun() throws MojoFailureException, MojoExecutionException {
    // create mocks
    CloudSdk cloudSdkMock = mock(CloudSdk.class);
    AppEngineDevServer devServerMock = mock(AppEngineDevServer.class);
    PluginDescriptor pluginDescriptorMock = CloudSdkMojoTest.createPluginDescriptorMock();
    CloudSdk.Builder cloudSdkBuilderMock = mock(CloudSdk.Builder.class, RETURNS_SELF);
    Factory factoryMock = mock(Factory.class);

    // create mojo
    RunMojo runMojo = new RunMojo();
    runMojo.factory = factoryMock;
    runMojo.pluginDescriptor = pluginDescriptorMock;

    // wire up
    when(factoryMock.devServer(cloudSdkMock)).thenReturn(devServerMock);
    when(factoryMock.cloudSdkBuilder()).thenReturn(cloudSdkBuilderMock);
    when(cloudSdkBuilderMock.build()).thenReturn(cloudSdkMock);

    // invoke
    runMojo.execute();

    // verify
    verify(devServerMock).run(runMojo);
    CloudSdkMojoTest.verifyCloudSdkCommon(runMojo, cloudSdkBuilderMock);
  }
}
