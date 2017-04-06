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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.maven.AppEngineFactory.SupportedVersion;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class StopMojoTest {

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @Mock
  private AppEngineDevServer devServerMock;

  @InjectMocks
  private StopMojo stopMojo;

  @Before
  public void setUp(){
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testStop(String version, SupportedVersion mockVersion)
      throws MojoFailureException, MojoExecutionException {

    // wire up
    stopMojo.devserverVersion = version;
    when(factoryMock.devServerStop(mockVersion)).thenReturn(devServerMock);

    // invoke
    stopMojo.execute();

    // verify
    verify(devServerMock).stop(stopMojo);
  }

}
