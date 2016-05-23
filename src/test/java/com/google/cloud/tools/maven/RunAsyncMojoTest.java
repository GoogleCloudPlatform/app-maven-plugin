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

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class RunAsyncMojoTest {

  @Test
  public void testRunAsync() throws MojoFailureException, MojoExecutionException {
    // create mocks
    CloudSdk cloudSdkMock = mock(CloudSdk.class);
    AppEngineDevServer devServerMock = mock(AppEngineDevServer.class);
    PluginDescriptor pluginDescriptorMock = CloudSdkMojoTest.createPluginDescriptorMock();
    Log logMock = mock(Log.class);

    // create spies
    RunAsyncMojo runAsyncMojoSpy = spy(RunAsyncMojo.class);
    CloudSdk.Builder cloudSdkBuilderSpy = spy(CloudSdk.Builder.class);

    // wire up
    doReturn(devServerMock).when(runAsyncMojoSpy).getDevServer(cloudSdkMock);
    doReturn(cloudSdkBuilderSpy).when(runAsyncMojoSpy).createCloudSdkBuilder();
    doReturn(cloudSdkMock).when(cloudSdkBuilderSpy).build();
    doReturn(logMock).when(runAsyncMojoSpy).getLog();
    runAsyncMojoSpy.pluginDescriptor = pluginDescriptorMock;
    runAsyncMojoSpy.startSuccessTimeout = 25;

    // invoke
    runAsyncMojoSpy.execute();

    // verify
    verify(devServerMock).run(runAsyncMojoSpy);
    verify(cloudSdkBuilderSpy).async(true);
    verify(cloudSdkBuilderSpy).runDevAppServerWait(25);
    CloudSdkMojoTest.verifyCloudSdkCommon(runAsyncMojoSpy, cloudSdkBuilderSpy);
    verify(logMock).info(contains("25 seconds"));
    verify(logMock).info(contains("started"));
  }
}
