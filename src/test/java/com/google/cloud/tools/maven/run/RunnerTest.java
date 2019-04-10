/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.operations.DevServer;
import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.cloudsdk.ConfigReader;
import com.google.cloud.tools.maven.run.Runner.ConfigBuilder;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunnerTest {

  private static final Path STANDARD_PROJECT_WEBAPP =
      Paths.get("src/test/resources/projects/standard-project/src/main/webapp");
  private static final Path STANDARD_PROJECT_WEBAPP2 =
      Paths.get("src/test/resources/projects/standard-project/src/main/webapp-for-services");
  private static final Path NON_STANDARD_PROJECT_WEBAPP =
      Paths.get("src/test/resources/projects/flexible-project/src/main/webapp");

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkAppEngineFactory appengineFactory;
  @Mock private DevServer devServer;
  @Mock private MavenProject mavenProject;
  @Mock private Log logMock;
  @Mock private ConfigReader configReader;
  @Mock private Gcloud gcloud;
  private Path appDir;

  @Mock private ConfigBuilder configBuilder;
  @Mock private AbstractRunMojo runMojo;
  @InjectMocks private Runner testRunner;

  @Before
  public void setUp() throws IOException {
    appDir = tempFolder.newFolder("artifact").toPath();
    when(mavenProject.getBuild()).thenReturn(mock(Build.class));
    when(mavenProject.getBuild().getDirectory())
        .thenReturn(appDir.getParent().toAbsolutePath().toString());
    when(mavenProject.getBuild().getFinalName()).thenReturn(appDir.getFileName().toString());
    when(runMojo.getMavenProject()).thenReturn(mavenProject);
    when(runMojo.getLog()).thenReturn(logMock);
    when(runMojo.getAppEngineFactory()).thenReturn(appengineFactory);
    when(appengineFactory.newConfigReader()).thenReturn(configReader);
  }

  private void setUpAppEngineWebXml() throws IOException {
    Path webInf = Files.createDirectory(appDir.resolve("WEB-INF"));
    Files.createFile(webInf.resolve("appengine-web.xml"));
  }

  @Test
  public void testRun() throws MojoExecutionException, IOException, AppEngineException {

    when(appengineFactory.devServerRunSync()).thenReturn(devServer);
    setUpAppEngineWebXml();

    testRunner.run();

    verify(devServer).run(configBuilder.buildRunConfiguration(ImmutableList.of(appDir), null));
  }

  @Test
  public void testRunAsync() throws MojoExecutionException, IOException, AppEngineException {
    final int START_SUCCESS_TIMEOUT = 25;

    when(appengineFactory.devServerRunAsync(START_SUCCESS_TIMEOUT)).thenReturn(devServer);
    setUpAppEngineWebXml();

    testRunner.runAsync(START_SUCCESS_TIMEOUT);

    verify(devServer).run(configBuilder.buildRunConfiguration(ImmutableList.of(appDir), null));
    verify(logMock).info(contains("25 seconds"));
    verify(logMock).info(contains("started"));
  }

  @Test
  public void testProcessServices_singleService() throws MojoExecutionException {
    List<Path> userConfiguredServices = ImmutableList.of(STANDARD_PROJECT_WEBAPP);
    when(runMojo.getServices()).thenReturn(userConfiguredServices);
    List<Path> processedServices = testRunner.processServices();
    Assert.assertEquals(userConfiguredServices, processedServices);
  }

  @Test
  public void testProcessServices_empty() throws MojoExecutionException {
    List<Path> userConfiguredServices = ImmutableList.of();
    when(runMojo.getServices()).thenReturn(userConfiguredServices);
    List<Path> processedServices = testRunner.processServices();
    Assert.assertEquals(ImmutableList.of(appDir), processedServices);
  }

  @Test
  public void testProcessServices_multipleProjects() throws MojoExecutionException {
    List<Path> userConfiguredServices =
        ImmutableList.of(STANDARD_PROJECT_WEBAPP, STANDARD_PROJECT_WEBAPP2);
    when(runMojo.getServices()).thenReturn(userConfiguredServices);
    List<Path> processedServices = testRunner.processServices();
    Assert.assertEquals(userConfiguredServices, processedServices);
  }

  @Test
  public void testProcessServices_multipleWithNonStandard() {
    List<Path> userConfiguredServices =
        ImmutableList.of(STANDARD_PROJECT_WEBAPP, NON_STANDARD_PROJECT_WEBAPP);
    when(runMojo.getServices()).thenReturn(userConfiguredServices);
    try {
      testRunner.processServices();
      fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(Runner.NON_STANDARD_APPLICATION_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessProjectId() {
    Mockito.when(runMojo.getProjectId()).thenReturn("some-project");
    String processedProjectId = testRunner.processProjectId();
    Assert.assertEquals("some-project", processedProjectId);
  }

  @Test
  public void testProcessProjectId_gcloud() {
    Mockito.when(configReader.getProjectId()).thenReturn("project-from-gcloud");
    Mockito.when(runMojo.getProjectId()).thenReturn(ConfigReader.GCLOUD_CONFIG);
    String processedProjectId = testRunner.processProjectId();
    Assert.assertEquals("project-from-gcloud", processedProjectId);
  }

  @Test
  public void testProcessProjectId_nullIgnored() {
    Assert.assertNull(testRunner.processProjectId());
  }

  @Test
  public void testRunnerFactory_appengineWebXml() throws IOException, MojoExecutionException {
    List<Path> services = ImmutableList.of(createAppEngineWebXmlApp());
    Mockito.when(runMojo.getServices()).thenReturn(services);

    Assert.assertNotNull(new Runner.Factory().newRunner(runMojo));
  }

  @Test
  public void testRunnerFactory_appYaml() throws IOException {
    List<Path> services = ImmutableList.of(createAppYamlApp());
    Mockito.when(runMojo.getServices()).thenReturn(services);

    try {
      new Runner.Factory().newRunner(runMojo);
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(
          "appengine:run is only available for appengine-web.xml based projects, the service defined in: "
              + services.get(0).toString()
              + " cannot be run by the dev appserver.",
          ex.getMessage());
    }
  }

  @Test
  public void testRunnerFactory_mixed() throws IOException {
    List<Path> services = ImmutableList.of(createAppEngineWebXmlApp(), createAppYamlApp());
    Mockito.when(runMojo.getServices()).thenReturn(services);

    try {
      new Runner.Factory().newRunner(runMojo);
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(
          "appengine:run is only available for appengine-web.xml based projects, the service defined in: "
              + services.get(1).toString()
              + " cannot be run by the dev appserver.",
          ex.getMessage());
    }
  }

  private Path createAppEngineWebXmlApp() throws IOException {
    Path appRoot = Files.createDirectory(tempFolder.getRoot().toPath().resolve("xml-app"));
    Path webinf = Files.createDirectory(appRoot.resolve("WEB-INF"));
    Files.createFile(webinf.resolve("appengine-web.xml"));

    return appRoot;
  }

  private Path createAppYamlApp() throws IOException {
    Path appRoot = Files.createDirectory(tempFolder.getRoot().toPath().resolve("yaml-app"));
    Files.createFile(appRoot.resolve("app.yaml"));

    return appRoot;
  }
}
