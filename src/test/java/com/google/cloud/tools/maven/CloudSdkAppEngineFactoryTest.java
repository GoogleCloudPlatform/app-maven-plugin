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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDevServer1;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDevServer2;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.InvalidJavaSdkException;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory.SupportedDevServerVersion;
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

  @Mock private CloudSdkOperationsFactory cloudSdkOperationsFactoryMock;
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

    doReturn(INSTALL_SDK_PATH).when(cloudSdkDownloader).downloadCloudSdk(logMock);
  }

  @Test
  public void testGetGcloud() {
    factory.getGcloud();
    verify(mojoMock).getArtifactId();
    verify(mojoMock).getArtifactVersion();
    verify(mojoMock).getServiceAccountKeyFile();
    verifyNoMoreInteractions(mojoMock);
  }

  @Test
  public void testCreateDevServerForVersion() {
    Assert.assertTrue(
        factory.createDevServerForVersion(SupportedDevServerVersion.V1, processHandler)
            instanceof CloudSdkAppEngineDevServer1);
    Assert.assertTrue(
        factory.createDevServerForVersion(SupportedDevServerVersion.V2ALPHA, processHandler)
            instanceof CloudSdkAppEngineDevServer2);
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithVersion()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, InvalidJavaSdkException,
          CloudSdkVersionFileException, AppEngineJavaComponentsNotInstalledException {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);
    when(cloudSdkOperationsFactoryMock.newDownloader(CLOUD_SDK_VERSION))
        .thenReturn(cloudSdkDownloader);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.defaultCloudSdk(mojoMock, cloudSdkOperationsFactoryMock);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadCloudSdk(logMock);
    verify(cloudSdkChecker, never()).checkCloudSdk(Mockito.any(CloudSdk.class));
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithoutVersion()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, InvalidJavaSdkException,
          CloudSdkVersionFileException, AppEngineJavaComponentsNotInstalledException {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    when(cloudSdkOperationsFactoryMock.newDownloader(null)).thenReturn(cloudSdkDownloader);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.defaultCloudSdk(mojoMock, cloudSdkOperationsFactoryMock);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadCloudSdk(logMock);
    verify(cloudSdkChecker, never()).checkCloudSdk(Mockito.any(CloudSdk.class));
  }

  @Test
  public void testDefaultCloudSdkBuilder_check()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, InvalidJavaSdkException,
          CloudSdkVersionFileException, AppEngineJavaComponentsNotInstalledException {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);
    when(cloudSdkOperationsFactoryMock.newChecker(CLOUD_SDK_VERSION)).thenReturn(cloudSdkChecker);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.defaultCloudSdk(mojoMock, cloudSdkOperationsFactoryMock);

    // verify
    Assert.assertEquals(CLOUD_SDK_HOME, sdk.getPath());
    verify(cloudSdkDownloader, never()).downloadCloudSdk(logMock);
    verify(cloudSdkChecker).checkCloudSdk(sdk);
  }

  @Test
  public void testDefaultCloudSdkBuilder_noCheck()
      throws CloudSdkOutOfDateException, CloudSdkNotFoundException, InvalidJavaSdkException,
          CloudSdkVersionFileException, AppEngineJavaComponentsNotInstalledException {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.defaultCloudSdk(mojoMock, cloudSdkOperationsFactoryMock);

    // verify
    Assert.assertEquals(CLOUD_SDK_HOME, sdk.getPath());
    verify(cloudSdkDownloader, never()).downloadCloudSdk(logMock);
    verify(cloudSdkChecker, never()).checkCloudSdk(Mockito.any(CloudSdk.class));
  }
}
