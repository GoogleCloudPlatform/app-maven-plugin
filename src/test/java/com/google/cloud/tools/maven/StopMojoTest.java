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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class StopMojoTest {

  @Test
  public void testStop() throws MojoFailureException, MojoExecutionException {
    // create mocks
    AppEngineDevServer devServerMock = mock(AppEngineDevServer.class);

    // create spies
    StopMojo stopMojoSpy = spy(StopMojo.class);

    // wire up
    doReturn(devServerMock).when(stopMojoSpy).getDevServer();

    // invoke
    stopMojoSpy.execute();

    // verify
    verify(devServerMock).stop(stopMojoSpy);
  }
}