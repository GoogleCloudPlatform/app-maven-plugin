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
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunAsyncMojoTest {

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @Mock
  private AppEngineDevServer devServerMock;

  @Mock
  private Log logMock;

  @InjectMocks
  private RunAsyncMojo runAsyncMojo;

  @Test
  public void testRunAsync() throws MojoFailureException, MojoExecutionException {
    final int START_SUCCESS_TIMEOUT = 25;

    // wire up
    when(factoryMock.devServerRunAsync(START_SUCCESS_TIMEOUT)).thenReturn(devServerMock);
    runAsyncMojo.startSuccessTimeout = START_SUCCESS_TIMEOUT;

    // invoke
    runAsyncMojo.execute();

    // verify
    verify(devServerMock).run(runAsyncMojo);
    verify(logMock).info(contains("25 seconds"));
    verify(logMock).info(contains("started"));
  }
}
