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

package com.google.cloud.tools.maven.cloudsdk;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.operations.CloudSdk;
import com.google.cloud.tools.appengine.operations.DevServerV1;
import com.google.cloud.tools.appengine.operations.DevServerV2;
import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory.SupportedDevServerVersion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.model.Build;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFactoryTest {

  private final Path CLOUD_SDK_HOME = Paths.get("google-cloud-sdk");
  private final Path INSTALL_SDK_PATH = Paths.get("installed-cloud-sdk");
  private final String CLOUD_SDK_VERSION = "192.0.0";
  private final String ARTIFACT_ID = "appengine-maven-plugin";
  private final String ARTIFACT_VERSION = "0.1.0";

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkMojo mojoMock;
  @Mock private MavenProject projectMock;
  @Mock private Build buildMock;
  @Mock private Log logMock;

  @Mock private CloudSdk cloudSdk;

  @Mock private CloudSdkDownloader cloudSdkDownloader;
  @Mock private CloudSdkChecker cloudSdkChecker;
  @Mock private ProcessHandler processHandler;

  @InjectMocks private CloudSdkAppEngineFactory factory;

  @Before
  public void wireUp() throws IOException {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    when(mojoMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(mojoMock.getArtifactVersion()).thenReturn(ARTIFACT_VERSION);
    when(mojoMock.getLog()).thenReturn(logMock);

    when(mojoMock.getMavenProject()).thenReturn(projectMock);
    when(projectMock.getBuild()).thenReturn(buildMock);
    File outFolder = tempFolder.newFolder("tempOut");
    when(buildMock.getDirectory()).thenReturn(outFolder.getAbsolutePath());

    doReturn(INSTALL_SDK_PATH)
        .when(cloudSdkDownloader)
        .downloadIfNecessary(
            Mockito.isNull(String.class), Mockito.eq(logMock), Mockito.anyBoolean());
    doReturn(INSTALL_SDK_PATH)
        .when(cloudSdkDownloader)
        .downloadIfNecessary(Mockito.anyString(), Mockito.eq(logMock), Mockito.anyBoolean());
  }

  @Test
  public void testGetGcloud() {
    factory.getGcloud();
    verify(mojoMock).getArtifactId();
    verify(mojoMock).getArtifactVersion();
    verify(mojoMock).getServiceAccountKeyFile();
  }

  @Test
  public void testCreateDevServerForVersion() {
    CloudSdkAppEngineFactory spyFactory = Mockito.spy(factory);
    doReturn(cloudSdk).when(spyFactory).buildCloudSdkWithAppEngineComponents();

    Assert.assertTrue(
        spyFactory.createDevServerForVersion(SupportedDevServerVersion.V1, processHandler)
            instanceof DevServerV1);
    Assert.assertTrue(
        spyFactory.createDevServerForVersion(SupportedDevServerVersion.V2ALPHA, processHandler)
            instanceof DevServerV2);
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadIfNecessary(CLOUD_SDK_VERSION, logMock, true);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithoutVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadIfNecessary(null, logMock, true);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testDefaultCloudSdkBuilder_checkNoAppEngine()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, CloudSdkVersionFileException {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(
            mojoMock, cloudSdkChecker, cloudSdkDownloader, false);

    // verify
    Assert.assertEquals(CLOUD_SDK_HOME, sdk.getPath());
    verify(cloudSdkChecker).checkCloudSdk(sdk, CLOUD_SDK_VERSION);
    verifyNoMoreInteractions(cloudSdkDownloader);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testDefaultCloudSdkBuilder_checkAppEngine()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, CloudSdkVersionFileException,
          AppEngineJavaComponentsNotInstalledException {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    // verify
    Assert.assertEquals(CLOUD_SDK_HOME, sdk.getPath());
    verify(cloudSdkChecker).checkCloudSdk(sdk, CLOUD_SDK_VERSION);
    verify(cloudSdkChecker).checkForAppEngine(sdk);
    verifyNoMoreInteractions(cloudSdkDownloader);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testDefaultCloudSdkBuilder_noCheck() {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(
            mojoMock, cloudSdkChecker, cloudSdkDownloader, false);

    // verify
    Assert.assertEquals(CLOUD_SDK_HOME, sdk.getPath());
    verifyNoMoreInteractions(cloudSdkDownloader);
    verifyNoMoreInteractions(cloudSdkChecker);
  }
}
