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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory.DefaultProcessOutputLineListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFactoryTest {

  private final Path CLOUD_SDK_PATH = Paths.get("google-cloud-sdk");
  private final String ARTIFACT_ID = "appengine-maven-plugin";
  private final String ARTIFACT_VERSION = "0.1.0";

  @Mock
  private CloudSdkMojo mojoMock;

  @Mock
  private CloudSdkAppEngineFactory.CloudSdkFactory cloudSdkFactoryMock;

  @Mock(answer = Answers.RETURNS_SELF)
  private CloudSdk.Builder cloudSdkBuilderMock;

  @Mock
  private CloudSdk cloudSdkMock;

  @InjectMocks
  private CloudSdkAppEngineFactory factory;


  @Before
  public void wireUp() {
    when(mojoMock.getCloudSdkPath()).thenReturn(CLOUD_SDK_PATH);
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
  public void testDevServerRunSync() {
    // invoke
    factory.devServerRunSync();

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServerRunAsync() {
    final int START_SUCCESS_TIMEOUT = 25;

    // invoke
    factory.devServerRunAsync(START_SUCCESS_TIMEOUT);

    // verify
    verify(cloudSdkBuilderMock).build();
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
    verify(cloudSdkBuilderMock).async(true);
    verify(cloudSdkBuilderMock).runDevAppServerWait(START_SUCCESS_TIMEOUT);
    verifyDefaultCloudSdkBuilder();
  }

  @Test
  public void testDevServerStop() {
    // invoke
    factory.devServerStop();

    // verify
    verify(cloudSdkFactoryMock).devServer(cloudSdkMock);
  }

  @Test
  public void testTestDefaultCloudSdkBuilder() {
    // invoke
    factory.defaultCloudSdkBuilder();

    // verify
    verifyDefaultCloudSdkBuilder();
  }

  private void verifyDefaultCloudSdkBuilder() {
    verify(cloudSdkBuilderMock).sdkPath(CLOUD_SDK_PATH);
    verify(cloudSdkBuilderMock).addStdOutLineListener(any(DefaultProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).addStdErrLineListener(any(DefaultProcessOutputLineListener.class));
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironment(ARTIFACT_ID);
    verify(cloudSdkBuilderMock).appCommandMetricsEnvironmentVersion(ARTIFACT_VERSION);
  }
}
