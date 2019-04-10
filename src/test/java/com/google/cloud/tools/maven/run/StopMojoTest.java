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

package com.google.cloud.tools.maven.run;

import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.StopConfiguration;
import com.google.cloud.tools.appengine.operations.DevServer;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StopMojoTest {

  @Mock private CloudSdkAppEngineFactory factoryMock;
  @Mock private DevServer devServerMock;

  @InjectMocks private StopMojo stopMojo;

  @Before
  public void setUp() throws MojoExecutionException {
    Mockito.when(factoryMock.devServerStop()).thenReturn(devServerMock);
  }

  @Test
  public void testStop() throws MojoExecutionException, AppEngineException {

    // wire up
    stopMojo.host = "host";
    stopMojo.port = 124;

    // invoke
    stopMojo.execute();

    // verify
    ArgumentCaptor<StopConfiguration> captor = ArgumentCaptor.forClass(StopConfiguration.class);
    verify(devServerMock).stop(captor.capture());

    Assert.assertEquals("host", captor.getValue().getHost());
    Assert.assertEquals(Integer.valueOf(124), captor.getValue().getPort());
  }

  @Test
  public void testExecute_skipTest() throws MojoExecutionException {
    stopMojo.setSkip(true);
    stopMojo.execute();
    Mockito.verifyNoMoreInteractions(devServerMock);
  }
}
