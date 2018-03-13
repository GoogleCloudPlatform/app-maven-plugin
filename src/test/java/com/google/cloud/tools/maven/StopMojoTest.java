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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class StopMojoTest {

  private static final String ADMIN_PORT = "2";
  private static final String PORT = "1";
  private static final String V1_VERSION = "1";
  private static final Object V2_VERSION = "2-alpha";

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @Mock
  private AppEngineDevServer devServerMock;

  @InjectMocks
  private StopMojo stopMojo;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp(){
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testInvalidVersionString()
      throws IOException, MojoExecutionException, MojoFailureException {
    stopMojo.devserverVersion = "bogus-version";
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Invalid version");

    stopMojo.execute();
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testStop(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException {

    // wire up
    stopMojo.devserverVersion = version;
    when(factoryMock.devServerStop(mockVersion)).thenReturn(devServerMock);

    // invoke
    stopMojo.execute();

    // verify
    verify(devServerMock).stop(stopMojo);
  }

  @Test
  @Parameters({"host,adminhost,1,host", "host,adminhost,2-alpha,adminhost"})
  public void testGetAdminHost(String host, String adminHost, String version, String expected)
      throws Exception {
    stopMojo.devserverVersion = version;
    stopMojo.adminHost = adminHost;
    stopMojo.host = host;

    assertEquals(expected, stopMojo.getAdminHost());
  }

  @Test
  @Parameters
  public void testGetAdminPort(Integer port, Integer adminPort, String version, Integer expected)
      throws Exception {
    stopMojo.devserverVersion = version;
    stopMojo.adminPort = adminPort;
    stopMojo.port = port;

    assertEquals(expected, stopMojo.getAdminPort());
  }

  @SuppressWarnings("unused") // used for testGetAdminPort()
  private Object[] parametersForTestGetAdminPort() {
    return new Object[] {
      new Object[]{ PORT, ADMIN_PORT, V1_VERSION, PORT },
      new Object[]{ PORT, ADMIN_PORT, V2_VERSION, ADMIN_PORT}
    };
  }
}
