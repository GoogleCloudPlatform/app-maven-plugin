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
import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFactoryTest {

  private final Path CLOUD_SDK_HOME = Paths.get("google-cloud-sdk");
  private final Path INSTALL_SDK_PATH = Paths.get("installed-cloud-sdk");
  private final String CLOUD_SDK_VERSION = "192.0.0";
  private final String ARTIFACT_ID = "appengine-maven-plugin";
  private final String ARTIFACT_VERSION = "0.1.0";

  @Mock private CloudSdkMojo mojoMock;
  @Mock private Log logMock;
  @Mock private MavenSession mavenSession;

  @Mock private CloudSdkDownloader cloudSdkDownloader;
  @Mock private CloudSdkChecker cloudSdkChecker;

  @InjectMocks private CloudSdkAppEngineFactory factory;

  @Before
  public void wireUp() {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    when(mojoMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(mojoMock.getArtifactVersion()).thenReturn(ARTIFACT_VERSION);
    when(mojoMock.getLog()).thenReturn(logMock);
    when(mojoMock.getMavenSession()).thenReturn(mavenSession);
    when(mavenSession.isOffline()).thenReturn(false);

    doReturn(INSTALL_SDK_PATH)
        .when(cloudSdkDownloader)
        .downloadIfNecessary(
            Mockito.isNull(), Mockito.eq(logMock), Mockito.anyBoolean(), Mockito.anyBoolean());
    doReturn(INSTALL_SDK_PATH)
        .when(cloudSdkDownloader)
        .downloadIfNecessary(
            Mockito.anyString(), Mockito.eq(logMock), Mockito.anyBoolean(), Mockito.anyBoolean());
  }

  @Test
  public void testGetGcloud() {
    factory.getGcloud();
    verify(mojoMock).getArtifactId();
    verify(mojoMock).getArtifactVersion();
    verify(mojoMock).getServiceAccountKeyFile();
  }

  @Test
  public void testBuildCloudSdk_downloadWithVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadIfNecessary(CLOUD_SDK_VERSION, logMock, true, false);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testBuildCloudSdk_downloadWithoutVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    // verify
    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadIfNecessary(null, logMock, true, false);
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testBuildCloudSdk_offlinePassthrough() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    when(mavenSession.isOffline()).thenReturn(true);

    // invoke
    CloudSdk sdk =
        CloudSdkAppEngineFactory.buildCloudSdk(mojoMock, cloudSdkChecker, cloudSdkDownloader, true);

    Assert.assertEquals(INSTALL_SDK_PATH, sdk.getPath());
    verify(cloudSdkDownloader).downloadIfNecessary(null, logMock, true, true);
    verify(mavenSession).isOffline();
    verifyNoMoreInteractions(cloudSdkChecker);
  }

  @Test
  public void testBuildCloudSdk_checkNoAppEngine()
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
  public void testBuildCloudSdk_checkAppEngine()
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
  public void testBuildCloudSdk_noCheck() {
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
