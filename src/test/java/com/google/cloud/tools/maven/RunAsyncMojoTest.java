/*
 * Copyright 2016 Google LLC. All Rights Reserved.
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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory.SupportedDevServerVersion;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class RunAsyncMojoTest extends AbstractDevServerTest {

  @Mock private Log logMock;

  @InjectMocks private RunAsyncMojo runAsyncMojo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testRunAsync(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException, AppEngineException {
    final int START_SUCCESS_TIMEOUT = 25;

    // wire up
    runAsyncMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    when(factoryMock.devServerRunAsync(START_SUCCESS_TIMEOUT, mockVersion))
        .thenReturn(devServerMock);
    runAsyncMojo.startSuccessTimeout = START_SUCCESS_TIMEOUT;

    // invoke
    runAsyncMojo.execute();

    // verify
    verify(devServerMock).run(runAsyncMojo);
    verify(logMock).info(contains("25 seconds"));
    verify(logMock).info(contains("started"));
  }
}
