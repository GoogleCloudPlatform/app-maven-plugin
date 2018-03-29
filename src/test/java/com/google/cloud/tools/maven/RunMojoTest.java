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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.RunConfiguration;
import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class RunMojoTest extends AbstractDevServerTest {

  @InjectMocks private RunMojo runMojo;

  @Captor
  private ArgumentCaptor<RunConfiguration> captor = ArgumentCaptor.forClass(RunConfiguration.class);

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testInvalidVersionString()
      throws IOException, MojoExecutionException, MojoFailureException {
    runMojo.devserverVersion = "bogus-version";
    setUpAppEngineWebXml();
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Invalid version");

    runMojo.execute();
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testRun(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException, AppEngineException {
    // wire up
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    // invoke
    runMojo.execute();

    // verify
    verify(devServerMock).run(runMojo);
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testRun_servicesIsUsed(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[] {new File("src/main/appengine")}, runMojo.getServices().toArray());
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testRunFlexible(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    runMojo.devserverVersion = version;
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);
    when(mavenProjectMock.getBuild()).thenReturn(mock(Build.class));
    when(mavenProjectMock.getBuild().getDirectory()).thenReturn("/fake/project/build/directory");
    when(mavenProjectMock.getBuild().getFinalName()).thenReturn("artifact");

    // invoke
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(
        "Dev App Server does not support App Engine Flexible Environment applications.");
    runMojo.execute();
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testEnvironment(String version, SupportedDevServerVersion mockVersion)
      throws IOException, MojoExecutionException, MojoFailureException, AppEngineException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    runMojo.environment = Collections.singletonMap("envVarName", "envVarValue");
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);
    doNothing().when(devServerMock).run(captor.capture());
    runMojo.execute();

    Map<String, String> environment = captor.getValue().getEnvironment();
    assertEquals(1, environment.size());
    assertEquals("envVarValue", environment.get("envVarName"));
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA"})
  public void testAdditionalArguments(String version, SupportedDevServerVersion mockVersion)
      throws IOException, MojoExecutionException, MojoFailureException, AppEngineException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    runMojo.additionalArguments = ImmutableList.of("--ARG1", "--ARG2");
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);
    doNothing().when(devServerMock).run(captor.capture());
    runMojo.execute();

    assertArrayEquals(
        new String[] {"--ARG1", "--ARG2"}, captor.getValue().getAdditionalArguments().toArray());
  }
}
