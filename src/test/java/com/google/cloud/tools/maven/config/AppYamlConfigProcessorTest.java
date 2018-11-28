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

package com.google.cloud.tools.maven.config;

import static org.junit.Assert.fail;

import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.maven.deploy.AbstractDeployMojo;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppYamlConfigProcessorTest {

  private static final String PROJECT_BUILD = "project-build";
  private static final String VERSION_BUILD = "version-build";

  @Mock private Gcloud gcloud;
  @Mock private ConfigReader configReader;
  @Mock private AbstractDeployMojo deployMojo;
  @Mock private MavenProject mavenProject;

  @InjectMocks private AppYamlConfigProcessor testProcessor;

  @Test
  public void testProcessProjectId_fromBuildConfig() {
    Assert.assertEquals(PROJECT_BUILD, testProcessor.processProjectId(PROJECT_BUILD));
  }

  @Test
  public void testProcessProjectId_fromGcloud() {
    Mockito.when(configReader.getProjectId(gcloud)).thenReturn("test-from-gcloud");
    Assert.assertEquals(
        "test-from-gcloud", testProcessor.processProjectId(ConfigProcessor.GCLOUD_CONFIG));
  }

  @Test
  public void testProcessProjectId_fromAppengineWebXml() {
    try {
      testProcessor.processProjectId(ConfigProcessor.APPENGINE_CONFIG);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppYamlConfigProcessor.PROJECT_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessProjectId_unset() {
    try {
      testProcessor.processProjectId(null);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppYamlConfigProcessor.PROJECT_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessVersion_fromBuildConfig() {
    Assert.assertEquals(VERSION_BUILD, testProcessor.processProjectId(VERSION_BUILD));
  }

  @Test
  public void testProcessVersion_fromGcloud() {
    Assert.assertNull(testProcessor.processVersion(ConfigProcessor.GCLOUD_CONFIG));
  }

  @Test
  public void testProcessVersion_fromAppengineWebXml() {
    try {
      testProcessor.processVersion(ConfigProcessor.APPENGINE_CONFIG);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppYamlConfigProcessor.VERSION_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessVersion_unset() {
    try {
      testProcessor.processVersion(null);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppYamlConfigProcessor.VERSION_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessAppEngineDirectory_userConfigured() {
    Path fakeAppEngineDirectory = Paths.get("fake", "appengine", "path");
    Mockito.when(deployMojo.getAppEngineDirectory()).thenReturn(fakeAppEngineDirectory);
    Assert.assertEquals(
        fakeAppEngineDirectory, testProcessor.processAppEngineDirectory(deployMojo));
  }

  @Test
  public void testProcessAppEngineDirectory_nullUsesDefault() {
    Path fakeProjectBasedir = Paths.get("some", "base", "dir");
    Mockito.when(deployMojo.getMavenProject()).thenReturn(mavenProject);
    Mockito.when(mavenProject.getBasedir()).thenReturn(fakeProjectBasedir.toFile());
    Assert.assertEquals(
        fakeProjectBasedir.resolve("src").resolve("main").resolve("appengine"),
        testProcessor.processAppEngineDirectory(deployMojo));
  }
}
