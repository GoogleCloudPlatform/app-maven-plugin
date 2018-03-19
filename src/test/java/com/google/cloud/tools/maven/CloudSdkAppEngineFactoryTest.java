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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory.DefaultProcessOutputLineListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFactoryTest {

  private final Path CLOUD_SDK_HOME = Paths.get("google-cloud-sdk");
  private final Path INSTALL_SDK_PATH = Paths.get("installed-cloud-sdk");
  private final String CLOUD_SDK_VERSION = "192.0.0";
  private final String ARTIFACT_ID = "appengine-maven-plugin";
  private final String ARTIFACT_VERSION = "0.1.0";

  @Mock private CloudSdkMojo mojoMock;

  @Mock private CloudSdkAppEngineFactory.CloudSdkFactory cloudSdkFactoryMock;

  @Mock(answer = Answers.RETURNS_SELF)
  private CloudSdk.Builder cloudSdkBuilderMock;

  @Mock private CloudSdk cloudSdkMock;

  @Spy @InjectMocks private CloudSdkAppEngineFactory factory;

  @Before
  public void wireUp() {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    when(mojoMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(mojoMock.getArtifactVersion()).thenReturn(ARTIFACT_VERSION);
    when(cloudSdkFactoryMock.cloudSdkBuilder()).thenReturn(cloudSdkBuilderMock);
    when(cloudSdkBuilderMock.build()).thenReturn(cloudSdkMock);
  }

  @Test
  public void testStandardStaging() {
    // invoke
    factory.standardStaging();

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).standardStaging(cloudSdkMock);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testFlexibleStaging() {
    // invoke
    factory.flexibleStaging();

    // verify
    verify(cloudSdkFactoryMock).flexibleStaging();
  }

  @Test
  public void testDeployment() {
    // invoke
    factory.deployment();

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).deployment(cloudSdkMock);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServer1RunSync() {
    // invoke
    factory.devServerRunSync(SupportedDevServerVersion.V1);

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer1(cloudSdkMock);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServer2RunSync() {
    // invoke
    factory.devServerRunSync(SupportedDevServerVersion.V2ALPHA);

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServer1RunAsync() {
    final int START_SUCCESS_TIMEOUT = 25;

    // invoke
    factory.devServerRunAsync(START_SUCCESS_TIMEOUT, SupportedDevServerVersion.V1);

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer1(cloudSdkMock);
    verify(cloudSdkBuilderMock).async(true);
    verify(cloudSdkBuilderMock).runDevAppServerWait(START_SUCCESS_TIMEOUT);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServer2RunAsync() {
    final int START_SUCCESS_TIMEOUT = 25;

    // invoke
    factory.devServerRunAsync(START_SUCCESS_TIMEOUT, SupportedDevServerVersion.V2ALPHA);

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
    verify(cloudSdkBuilderMock).async(true);
    verify(cloudSdkBuilderMock).runDevAppServerWait(START_SUCCESS_TIMEOUT);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServer1Stop() {
    // invoke
    factory.devServerStop(SupportedDevServerVersion.V1);

    // verify
    verify(cloudSdkFactoryMock).devServer1(cloudSdkMock);
  }

  @Test
  public void testDevServer2Stop() {
    // invoke
    factory.devServerStop(SupportedDevServerVersion.V2ALPHA);

    // verify
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);
    doReturn(INSTALL_SDK_PATH).when(factory).downloadCloudSdk();
    doNothing().when(factory).checkCloudSdk();

    // invoke
    factory.defaultCloudSdkBuilder();

    // verify
    verifyDefaultCloudSdkBuilder(INSTALL_SDK_PATH);
    verify(factory).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  @Test
  public void testDefaultCloudSdkBuilder_downloadWithoutVersion() {
    when(mojoMock.getCloudSdkHome()).thenReturn(null);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    doReturn(INSTALL_SDK_PATH).when(factory).downloadCloudSdk();
    doNothing().when(factory).checkCloudSdk();

    // invoke
    factory.defaultCloudSdkBuilder();

    // verify
    verifyDefaultCloudSdkBuilder(INSTALL_SDK_PATH);
    verify(factory).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  @Test
  public void testDefaultCloudSdkBuilder_check() {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(CLOUD_SDK_VERSION);
    doReturn(INSTALL_SDK_PATH).when(factory).downloadCloudSdk();
    doNothing().when(factory).checkCloudSdk();

    // invoke
    factory.defaultCloudSdkBuilder();

    // verify
    verifyDefaultCloudSdkBuilder();
    verify(factory, never()).downloadCloudSdk();
    verify(factory).checkCloudSdk();
  }

  @Test
  public void testDefaultCloudSdkBuilder_noCheck() {
    when(mojoMock.getCloudSdkHome()).thenReturn(CLOUD_SDK_HOME);
    when(mojoMock.getCloudSdkVersion()).thenReturn(null);
    doReturn(INSTALL_SDK_PATH).when(factory).downloadCloudSdk();
    doNothing().when(factory).checkCloudSdk();

    // invoke
    factory.defaultCloudSdkBuilder();

    // verify
    verifyDefaultCloudSdkBuilder();
    verify(factory, never()).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  private void verifyDefaultCloudSdkBuilder() {
    verifyDefaultCloudSdkBuilder(CLOUD_SDK_HOME);
  }

  private void verifyDefaultCloudSdkBuilder(Path cloudSdkPath) {
    verify(cloudSdkBuilderMock).sdkPath(cloudSdkPath);
    verify(cloudSdkBuilderMock).addStdOutLineListener(any(DefaultProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).addStdErrLineListener(any(DefaultProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironment(ARTIFACT_ID);
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironmentVersion(ARTIFACT_VERSION);
  }
}
