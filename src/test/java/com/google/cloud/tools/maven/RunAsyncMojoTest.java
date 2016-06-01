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
import static org.mockito.Mockito.verify;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunAsyncMojoTest extends CloudSdkMojoTest {

  @InjectMocks
  private RunAsyncMojo runAsyncMojo;

  @Test
  public void testRunAsync() throws MojoFailureException, MojoExecutionException {

    runAsyncMojo.pluginDescriptor = pluginDescriptorMock;
    runAsyncMojo.startSuccessTimeout = 25;

    // invoke
    runAsyncMojo.execute();

    // verify
    verify(devServerMock).run(runAsyncMojo);
    verify(cloudSdkBuilderMock).async(true);
    verify(cloudSdkBuilderMock).runDevAppServerWait(25);
    CloudSdkMojoTest.verifyCloudSdkCommon(runAsyncMojo, cloudSdkBuilderMock);
    verify(logMock).info(contains("25 seconds"));
    verify(logMock).info(contains("started"));
  }
}
