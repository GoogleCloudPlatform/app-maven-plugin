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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
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
    when(mavenProjectMock.getPlugin("com.google.cloud.tools:appengine-maven-plugin"))
        .thenReturn(mockPlugin);
    when(mockPlugin.getConfiguration()).thenReturn(mockConfiguration);
  }

  @Test
  public void testRun() throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    setUpAppEngineWebXml();
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    // invoke
    runMojo.execute();

    // verify
    verify(devServerMock).run(runMojo);
  }

  @Test
  public void testRun_appYamlsSetAndOverridesServices()
      throws MojoFailureException, MojoExecutionException, IOException {
    setUpAppEngineWebXml();
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  public void testRun_appYamlsNotSetServicesIsUsed()
      throws MojoFailureException, MojoExecutionException, IOException {
    setUpAppEngineWebXml();
    runMojo.appYamls = null;
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  public void testRun_appYamlsEmptyServicesIsUsed()
      throws MojoFailureException, MojoExecutionException, IOException {
    setUpAppEngineWebXml();
    runMojo.appYamls = Collections.emptyList();
    runMojo.services = Collections.singletonList(new File("src/main/appengine"));
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    runMojo.execute();

    assertArrayEquals(new File[]{ new File("src/main/appengine") },
        runMojo.getServices().toArray());
  }

  @Test
  public void testRun_appYamlAndServicesSetCausesError()
      throws MojoFailureException, MojoExecutionException, IOException {
    when(mockConfiguration.getChild("services")).thenReturn(mock(Xpp3Dom.class));
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    runMojo.services = Collections.singletonList(new File("src/main/appengine2"));
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Both <appYamls> and <services> are defined."
        + " <appYamls> is deprecated, use <services> only.");

    runMojo.execute();
  }

  @Test
  public void testRun_unexpectedConfigurationClass()
      throws MojoFailureException, MojoExecutionException, IOException {
    when(mockConfiguration.getChild("services")).thenReturn(mock(Xpp3Dom.class));
    runMojo.appYamls = Collections.singletonList(new File("src/main/appengine"));
    when(mockPlugin.getConfiguration()).thenReturn(new Object());

    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Unexpected configuration object, report this error on "
        + "https://github.com/GoogleCloudPlatform/app-maven-plugin/issues");

    runMojo.execute();
  }

  @Test
  public void testRunFlexible() throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    // invoke
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(
        "Dev App Server does not support App Engine Flexible Environment applications.");
    runMojo.execute();
  }
}
