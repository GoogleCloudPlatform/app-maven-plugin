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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class RunMojoTest extends AbstractDevServerTest {

  @Mock
  private Plugin mockPlugin;
  @Mock
  private Xpp3Dom mockConfiguration;
  @InjectMocks
  private RunMojo runMojo;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(mavenProjectMock.getPlugin("com.google.cloud.tools:appengine-maven-plugin"))
        .thenReturn(mockPlugin);
    when(mockPlugin.getConfiguration()).thenReturn(mockConfiguration);
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
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRun(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
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
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRun_appYamlsSetAndOverridesServices(String version,
      SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRun_appYamlsNotSetServicesIsUsed(String version,
      SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRun_appYamlsEmptyServicesIsUsed(String version,
      SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    setUpAppEngineWebXml();
    runMojo.appYamls = Collections.emptyList();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRun_appYamlAndServicesSetCausesError(String version,
      SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    when(mockConfiguration.getChild("services")).thenReturn(mock(Xpp3Dom.class));
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    runMojo.services = Collections.singletonList(new File("src/main/appengine2"));
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Both <appYamls> and <services> are defined."
        + " <appYamls> is deprecated, use <services> only.");

    runMojo.execute();
  }

  @Test
  @Parameters({"1", "2-alpha" })
  public void testRun_unexpectedConfigurationClass(String version)
      throws MojoFailureException, MojoExecutionException, IOException {
    runMojo.devserverVersion = version;
    when(mockConfiguration.getChild("services")).thenReturn(mock(Xpp3Dom.class));
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    when(mockPlugin.getConfiguration()).thenReturn(new Object());

    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Unexpected configuration object, report this error on "
        + "https://github.com/GoogleCloudPlatform/app-maven-plugin/issues");

    runMojo.execute();
  }

  @Test
  @Parameters({"1,V1", "2-alpha,V2ALPHA" })
  public void testRunFlexible(String version, SupportedDevServerVersion mockVersion)
      throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    runMojo.devserverVersion = version;
    when(factoryMock.devServerRunSync(mockVersion)).thenReturn(devServerMock);

    // invoke
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(
        "Dev App Server does not support App Engine Flexible Environment applications.");
    runMojo.execute();
  }
}
