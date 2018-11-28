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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.api.devserver.StopConfiguration;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory.SupportedDevServerVersion;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class StopMojoTest {

  private static final String ADMIN_PORT = "2";
  private static final String PORT = "1";
  private static final String V1_VERSION = "1";
  private static final Object V2_VERSION = "2-alpha";

  @Mock private CloudSdkAppEngineFactory factoryMock;

  @Mock private AppEngineDevServer devServerMock;

  @InjectMocks private StopMojo stopMojo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testInvalidVersionString() {
    stopMojo.devserverVersion = "bogus-version";

    try {
      stopMojo.execute();
      fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals("Invalid version", ex.getMessage());
    }
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testStop(String version, SupportedDevServerVersion mockVersion)
      throws MojoExecutionException, AppEngineException {

    // wire up
    stopMojo.adminHost = "adminHost";
    stopMojo.adminPort = 123;
    stopMojo.host = "host";
    stopMojo.port = 124;
    stopMojo.devserverVersion = version;
    when(factoryMock.devServerStop(mockVersion)).thenReturn(devServerMock);

    // invoke
    stopMojo.execute();

    // verify
    ArgumentCaptor<StopConfiguration> captor = ArgumentCaptor.forClass(StopConfiguration.class);
    verify(devServerMock).stop(captor.capture());

    Assert.assertEquals(stopMojo.processAdminHost(), captor.getValue().getAdminHost());
    Assert.assertEquals(stopMojo.processAdminPort(), captor.getValue().getAdminPort());
  }

  @Test
  @Parameters({"host,adminhost,1,host", "host,adminhost,2-alpha,adminhost"})
  public void testGetAdminHost(String host, String adminHost, String version, String expected) {
    stopMojo.devserverVersion = version;
    stopMojo.adminHost = adminHost;
    stopMojo.host = host;

    assertEquals(expected, stopMojo.processAdminHost());
  }

  @Test
  @Parameters
  public void testGetAdminPort(Integer port, Integer adminPort, String version, Integer expected) {
    stopMojo.devserverVersion = version;
    stopMojo.adminPort = adminPort;
    stopMojo.port = port;

    assertEquals(expected, stopMojo.processAdminPort());
  }

  @SuppressWarnings("unused") // used for testGetAdminPort()
  private Object[] parametersForTestGetAdminPort() {
    return new Object[] {
      new Object[] {PORT, ADMIN_PORT, V1_VERSION, PORT},
      new Object[] {PORT, ADMIN_PORT, V2_VERSION, ADMIN_PORT}
    };
  }
}
