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

package com.google.cloud.tools.maven.stage;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.configuration.AppEngineWebXmlProjectStageConfiguration;
import com.google.cloud.tools.appengine.operations.AppEngineWebXmlProjectStaging;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.stage.AppEngineWebXmlStager.ConfigBuilder;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class AppEngineWebXmlStagerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkAppEngineFactory appengineFactory;
  @Mock private AppEngineWebXmlProjectStaging staging;
  @Mock private Log logMock;
  @Mock private AppEngineWebXmlProjectStageConfiguration stagingConfiguration;

  @Mock private ConfigBuilder configBuilder;
  @Mock private AbstractStageMojo stageMojo;

  @InjectMocks private AppEngineWebXmlStager testStager;

  @Before
  public void configureStageMojo() throws MojoExecutionException {
    MockitoAnnotations.initMocks(this);
    when(stageMojo.getLog()).thenReturn(logMock);
    when(stageMojo.getAppEngineFactory()).thenReturn(appengineFactory);
    when(appengineFactory.appengineWebXmlStaging()).thenReturn(staging);
    when(configBuilder.buildConfiguration()).thenReturn(stagingConfiguration);
    when(stagingConfiguration.getStagingDirectory()).thenReturn(tempFolder.getRoot().toPath());
  }

  @Test
  public void testStage() throws Exception {

    // invoke
    testStager.stage();

    // verify
    verify(appengineFactory).appengineWebXmlStaging();
    verify(staging).stageStandard(stagingConfiguration);
    verify(logMock).info("Detected App Engine appengine-web.xml based application.");
  }

  @Test
  @Parameters({
    "dockerfile|dockerfile1|dockerfile2|dockerfile",
    "|dockerfile1|dockerfile2|dockerfile1",
    "||dockerfile2|dockerfile2"
  })
  public void testProcessDockerfile_passthrough(
      String dockerfileName, String dockerfile1Name, String dockerfile2Name, String expectedName)
      throws IOException {
    Path testRoot = tempFolder.getRoot().toPath();
    Path dockerfile =
        Strings.isNullOrEmpty(dockerfileName)
            ? null
            : Files.createFile(testRoot.resolve(dockerfileName));
    Path dockerfile1 =
        Strings.isNullOrEmpty(dockerfile1Name)
            ? null
            : Files.createFile(testRoot.resolve(dockerfile1Name));
    Path dockerfile2 =
        Strings.isNullOrEmpty(dockerfile2Name)
            ? null
            : Files.createFile(testRoot.resolve(dockerfile2Name));
    Mockito.when(stageMojo.getDockerfile()).thenReturn(dockerfile);
    Mockito.when(stageMojo.getDockerfilePrimaryDefaultLocation()).thenReturn(dockerfile1);
    Mockito.when(stageMojo.getDockerfileSecondaryDefaultLocation()).thenReturn(dockerfile2);

    Path processedDockerfile = new ConfigBuilder(stageMojo).processDockerfile();
    Assert.assertEquals(testRoot.resolve(expectedName), processedDockerfile);
  }

  /** Configure an appengine-web.xml for these test. */
  private Path setupSourceDirectory(String extraContent) throws IOException {
    Path appengineWebXml =
        Files.createFile(tempFolder.getRoot().toPath().resolve("appengine-web.xml"));
    Files.write(
        appengineWebXml,
        ("<appengine-web-app>" + extraContent + "</appengine-web-app>")
            .getBytes(StandardCharsets.UTF_8));
    when(stageMojo.getAppEngineWebXml()).thenReturn(appengineWebXml);
    return appengineWebXml;
  }

  @Test
  @Parameters({"1.7", "1.8"})
  public void testProcessRuntime_nullPassthrough(String compileTargetVersion)
      throws IOException, MojoExecutionException {
    setupSourceDirectory("");
    when(stageMojo.getCompileTargetVersion()).thenReturn(compileTargetVersion);

    String processedRuntime = new ConfigBuilder(stageMojo).processRuntime();
    Assert.assertNull(processedRuntime);
  }

  @Test
  public void testProcessRuntime_ignoreOverride17() throws IOException, MojoExecutionException {
    setupSourceDirectory("<vm>true</vm>");
    when(stageMojo.getCompileTargetVersion()).thenReturn("1.7");

    String processedRuntime = new ConfigBuilder(stageMojo).processRuntime();
    Assert.assertEquals(null, processedRuntime);
  }

  @Test
  public void testProcessRuntime_overrideWithJava() throws IOException, MojoExecutionException {
    setupSourceDirectory("<vm>true</vm>");
    // TODO: Add test for 1.8+
    when(stageMojo.getCompileTargetVersion()).thenReturn("1.8");

    String processedRuntime = new ConfigBuilder(stageMojo).processRuntime();
    Assert.assertEquals("java", processedRuntime);
  }
}
