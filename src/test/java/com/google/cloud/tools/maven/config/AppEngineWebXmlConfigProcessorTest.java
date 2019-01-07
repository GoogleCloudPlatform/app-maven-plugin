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

import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.maven.deploy.AbstractDeployMojo;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppEngineWebXmlConfigProcessorTest {
  private static final String PROJECT_BUILD = "project-build";
  private static final String VERSION_BUILD = "version-build";

  @Mock private Gcloud gcloud;
  @Mock private Path appengineWebXml;
  @Mock private ConfigReader configReader;
  @Mock private AbstractDeployMojo deployMojo;

  @InjectMocks private AppEngineWebXmlConfigProcessor testProcessor;

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
    Mockito.when(configReader.getProjectId(appengineWebXml)).thenReturn("test-from-xml");
    Assert.assertEquals(
        "test-from-xml", testProcessor.processProjectId(ConfigProcessor.APPENGINE_CONFIG));
  }

  @Test
  public void testProcessProjectId_unset() {
    try {
      testProcessor.processProjectId(null);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppEngineWebXmlConfigProcessor.PROJECT_ERROR, ex.getMessage());
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
    Mockito.when(configReader.getVersion(appengineWebXml)).thenReturn("test-from-xml");
    Assert.assertEquals(
        "test-from-xml", testProcessor.processVersion(ConfigProcessor.APPENGINE_CONFIG));
  }

  @Test
  public void testProcessVersion_unset() {
    try {
      testProcessor.processVersion(null);
      fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppEngineWebXmlConfigProcessor.VERSION_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testProcessAppEngineDirectory() {
    Path fakeStagingPath = Paths.get("fake", "staging", "path");
    Mockito.when(deployMojo.getStagingDirectory()).thenReturn(fakeStagingPath);
    Assert.assertEquals(
        fakeStagingPath.resolve("WEB-INF").resolve("appengine-generated"),
        testProcessor.processAppEngineDirectory(deployMojo));
  }
}
