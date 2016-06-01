/*
 * Copyright (c) 2016 Google Inc. All Right Reserved.
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

import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class StageMojoTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testStandardStaging() throws Exception {
    // create mocks
    CloudSdk cloudSdkMock = mock(CloudSdk.class);
    CloudSdkAppEngineStandardStaging standardStagingMock = mock(
        CloudSdkAppEngineStandardStaging.class);
    PluginDescriptor pluginDescriptorMock = CloudSdkMojoTest.createPluginDescriptorMock();
    Log logMock = mock(Log.class);
    CloudSdk.Builder cloudSdkBuilderMock = mock(CloudSdk.Builder.class, RETURNS_SELF);
    Factory factoryMock = mock(Factory.class);

    // create mojo
    StageMojo stageMojo = new StageMojo();
    stageMojo.factory = factoryMock;
    stageMojo.pluginDescriptor = pluginDescriptorMock;
    stageMojo.stagingDirectory = tempFolder.newFolder("staging");
    stageMojo.sourceDirectory = tempFolder.newFolder("source");

    // wire up
    when(factoryMock.standardStaging(cloudSdkMock)).thenReturn(standardStagingMock);
    when(factoryMock.logger(stageMojo)).thenReturn(logMock);
    when(factoryMock.cloudSdkBuilder()).thenReturn(cloudSdkBuilderMock);
    when(cloudSdkBuilderMock.build()).thenReturn(cloudSdkMock);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();

    // invoke
    stageMojo.execute();

    // verify
    verify(standardStagingMock).stageStandard(stageMojo);
    verify(logMock).info(contains("standard"));
    CloudSdkMojoTest.verifyCloudSdkCommon(stageMojo, cloudSdkBuilderMock);
  }

  @Test
  public void testFlexibleStaging() throws Exception {
    // create mocks
    Factory factoryMock = mock(Factory.class);
    CloudSdkAppEngineFlexibleStaging flexibleStagingMock = mock(
        CloudSdkAppEngineFlexibleStaging.class);
    Log logMock = mock(Log.class);

    // create mojo
    StageMojo stageMojo = new StageMojo();
    stageMojo.factory = factoryMock;
    stageMojo.stagingDirectory = tempFolder.newFolder("staging");
    stageMojo.sourceDirectory = tempFolder.newFolder("source");

    // wire up
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.logger(stageMojo)).thenReturn(logMock);

    // invoke
    stageMojo.execute();

    // verify
    verify(flexibleStagingMock).stageFlexible(stageMojo);
    verify(logMock).info(contains("flexible"));
  }

}
