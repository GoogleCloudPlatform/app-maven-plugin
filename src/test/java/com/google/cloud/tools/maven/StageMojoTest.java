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

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.app.api.deploy.AppEngineStandardStaging;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class StageMojoTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @Mock
  private AppEngineFlexibleStaging flexibleStagingMock;

  @Mock
  private AppEngineStandardStaging standardStagingMock;

  @Mock
  private Log logMock;

  @InjectMocks
  private StageMojo stageMojo;

  @Before
  public void configureStageMojo() throws IOException {
    stageMojo.stagingDirectory = tempFolder.newFolder("staging");
    stageMojo.sourceDirectory = tempFolder.newFolder("source");
  }

  @Test
  public void testStandardStaging() throws Exception {

    // wire up
    when(factoryMock.standardStaging()).thenReturn(standardStagingMock);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();

    // invoke
    stageMojo.execute();

    // verify
    verify(standardStagingMock).stageStandard(stageMojo);
    verify(logMock).info(contains("standard"));
  }

  @Test
  public void testFlexibleStaging() throws Exception {

    // wire up
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);

    // invoke
    stageMojo.execute();

    // verify
    verify(flexibleStagingMock).stageFlexible(stageMojo);
    verify(logMock).info(contains("flexible"));
  }

}
