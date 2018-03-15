/*
 * Copyright 2016 Google LLC. All Rights Reserved. All Right Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkMojoTest {

  private final File CLOUD_SDK_PATH = Paths.get("google-cloud-sdk").toFile();
  private final String CLOUD_SDK_VERSION = "192.0.0";

  @Mock private PluginDescriptor pluginDescriptorMock;

  @Mock private MavenProject mavenProject;

  @Mock private CloudSdkAppEngineFactory factory;

  @InjectMocks private CloudSdkMojoImpl mojo;

  @Before
  public void setup() {
    doNothing().when(factory).downloadCloudSdk();
    doNothing().when(factory).checkCloudSdk();
  }

  @Test
  public void testExecute_downloadVersion() throws MojoFailureException, MojoExecutionException {
    mojo.setCloudSdkPath(null);
    mojo.setCloudSdkVersion(CLOUD_SDK_VERSION);

    mojo.execute();

    verify(factory).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  @Test
  public void testExecute_downloadLatest() throws MojoFailureException, MojoExecutionException {
    mojo.setCloudSdkPath(null);
    mojo.setCloudSdkVersion(null);

    mojo.execute();

    verify(factory).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  @Test
  public void testExecute_check() throws MojoFailureException, MojoExecutionException {
    mojo.setCloudSdkPath(CLOUD_SDK_PATH);
    mojo.setCloudSdkVersion(CLOUD_SDK_VERSION);

    mojo.execute();

    verify(factory, never()).downloadCloudSdk();
    verify(factory).checkCloudSdk();
  }

  @Test
  public void testExecute_noCheck() throws MojoFailureException, MojoExecutionException {
    mojo.setCloudSdkPath(CLOUD_SDK_PATH);
    mojo.setCloudSdkVersion(null);

    mojo.execute();

    verify(factory, never()).downloadCloudSdk();
    verify(factory, never()).checkCloudSdk();
  }

  @Test
  public void testGetArtifactId() {
    final String ARTIFACT_ID = "appengine-maven-plugin";

    // wire up
    when(pluginDescriptorMock.getArtifactId()).thenReturn(ARTIFACT_ID);

    // invoke & verify
    assertEquals(ARTIFACT_ID, mojo.getArtifactId());
  }

  @Test
  public void testGetArtifactVersion() {
    final String ARTIFACT_VERSION = "0.1.0";

    // wire up
    when(pluginDescriptorMock.getVersion()).thenReturn(ARTIFACT_VERSION);

    // invoke & verify
    assertEquals(ARTIFACT_VERSION, mojo.getArtifactVersion());
  }

  @Test
  public void testGetPackaging() throws Exception {
    when(mavenProject.getPackaging()).thenReturn("this-is-a-test-packaging");

    assertEquals("this-is-a-test-packaging", mojo.getPackaging());
  }

  static class CloudSdkMojoImpl extends CloudSdkMojo {}
}
