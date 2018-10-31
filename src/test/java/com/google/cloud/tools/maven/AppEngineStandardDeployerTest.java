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

package com.google.cloud.tools.maven;

import static com.google.cloud.tools.maven.AppEngineDeployer.APPENGINE_CONFIG;
import static com.google.cloud.tools.maven.AppEngineDeployer.GCLOUD_CONFIG;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class AppEngineStandardDeployerTest {
  private static final String PROJECT_BUILD = "project-build";
  private static final String PROJECT_XML = "project-xml";
  private static final String PROJECT_GCLOUD = "project-gcloud";
  private static final String VERSION_BUILD = "version-build";
  private static final String VERSION_XML = "version-xml";

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  private File appengineWebXml;

  private AbstractDeployMojo deployMojo;
  @Mock PluginDescriptor pluginDescriptorMock;
  @Mock CloudSdkAppEngineFactory factoryMock;
  @Mock Gcloud gcloudMock;
  @Mock CloudSdkConfig cloudSdkConfigMock;
  private AppEngineStandardDeployer appEngineStandardDeployer;

  @Before
  public void setup()
      throws IOException, CloudSdkNotFoundException, ProcessHandlerException,
          CloudSdkOutOfDateException, CloudSdkVersionFileException {
    MockitoAnnotations.initMocks(this);
    deployMojo = new DeployMojo();

    deployMojo.pluginDescriptor = pluginDescriptorMock;
    Mockito.when(pluginDescriptorMock.getArtifactId()).thenReturn("junk");
    Mockito.when(pluginDescriptorMock.getVersion()).thenReturn("junk");
    deployMojo.factory = factoryMock;
    Mockito.when(factoryMock.getGcloud()).thenReturn(gcloudMock);
    Mockito.when(gcloudMock.getConfig()).thenReturn(cloudSdkConfigMock);
    Mockito.when(cloudSdkConfigMock.getProject()).thenReturn(PROJECT_GCLOUD);

    deployMojo.sourceDirectory = tempFolder.newFolder("source");
    deployMojo.setStagingDirectory(tempFolder.newFolder("staging"));
    deployMojo.setProjectId("some-project");
    deployMojo.setVersion("some-version");
    appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appEngineStandardDeployer = new AppEngineStandardDeployer(deployMojo);
  }

  @Test
  public void testUpdateGcloudProperties_fromBuildConfig() throws IOException {
    createAppEngineWebXml(true, true);
    deployMojo.setVersion(VERSION_BUILD);
    deployMojo.setProjectId(PROJECT_BUILD);
    appEngineStandardDeployer.setDeploymentProjectAndVersion();
    Assert.assertEquals(VERSION_BUILD, deployMojo.getVersion());
    Assert.assertEquals(PROJECT_BUILD, deployMojo.getProjectId());
  }

  @Test
  public void testUpdateGcloudProperties_fromAppengineWebXml() throws IOException {
    createAppEngineWebXml(true, true);
    deployMojo.setVersion(APPENGINE_CONFIG);
    deployMojo.setProjectId(APPENGINE_CONFIG);
    appEngineStandardDeployer.setDeploymentProjectAndVersion();
    Assert.assertEquals(VERSION_XML, deployMojo.getVersion());
    Assert.assertEquals(PROJECT_XML, deployMojo.getProjectId());
  }

  @Test
  public void testUpdateGcloudProperties_fromGcloud() throws IOException {
    createAppEngineWebXml(true, true);
    deployMojo.setVersion(GCLOUD_CONFIG);
    deployMojo.setProjectId(GCLOUD_CONFIG);

    appEngineStandardDeployer.setDeploymentProjectAndVersion();
    Assert.assertEquals(PROJECT_GCLOUD, deployMojo.getProjectId());
    Assert.assertEquals(null, deployMojo.getVersion());
  }

  @Test
  public void testUpdateGcloudProperties_fromAppengineWebXmlNoApplication() throws IOException {
    createAppEngineWebXml(false, true);
    deployMojo.setVersion(APPENGINE_CONFIG);
    deployMojo.setProjectId(APPENGINE_CONFIG);
    try {
      appEngineStandardDeployer.setDeploymentProjectAndVersion();
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals("<application> was not found in appengine-web.xml", ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_fromAppengineWebXmlNoVersion() throws IOException {
    createAppEngineWebXml(true, false);
    deployMojo.setVersion(APPENGINE_CONFIG);
    deployMojo.setProjectId(APPENGINE_CONFIG);
    try {
      appEngineStandardDeployer.setDeploymentProjectAndVersion();
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals("<version> was not found in appengine-web.xml", ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_noProjectSet() throws IOException {
    createAppEngineWebXml(true, true);
    deployMojo.setVersion(VERSION_BUILD);
    deployMojo.setProjectId(null);
    try {
      appEngineStandardDeployer.setDeploymentProjectAndVersion();
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppEngineStandardDeployer.PROJECT_ERROR, ex.getMessage());
    }
  }

  @Test
  public void testUpdateGcloudProperties_noVersionSet() throws IOException {
    createAppEngineWebXml(true, true);
    deployMojo.setVersion(null);
    deployMojo.setProjectId(PROJECT_BUILD);
    try {
      appEngineStandardDeployer.setDeploymentProjectAndVersion();
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(AppEngineStandardDeployer.VERSION_ERROR, ex.getMessage());
    }
  }

  private void createAppEngineWebXml(boolean withProject, boolean withVersion) throws IOException {
    appengineWebXml.createNewFile();
    Files.asCharSink(appengineWebXml, Charsets.UTF_8)
        .write(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
                + (withProject ? "<application>" + PROJECT_XML + "</application>" : "")
                + (withVersion ? "<version>" + VERSION_XML + "</version>" : "")
                + "</appengine-web-app>");
  }
}
