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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkMojoTest {

  @Mock
  private PluginDescriptor pluginDescriptorMock;

  @Mock
  private MavenProject mavenProject;

  @InjectMocks
  private CloudSdkMojoImpl mojo;

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

  static class CloudSdkMojoImpl extends CloudSdkMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
  }
}
