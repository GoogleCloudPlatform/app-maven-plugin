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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class StageMojoTest extends CloudSdkMojoTest {

  @InjectMocks
  private StageMojo stageMojo;

  @Before
  public void configureStageMojo() throws IOException {
    stageMojo.stagingDirectory = tempFolder.newFolder("staging");
    stageMojo.sourceDirectory = tempFolder.newFolder("source");
  }

  @Test
  public void testStandardStaging() throws Exception {

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

    // invoke
    stageMojo.execute();

    // verify
    verify(flexibleStagingMock).stageFlexible(stageMojo);
    verify(logMock).info(contains("flexible"));
  }

}
