/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.maven.stage;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StagerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private AbstractStageMojo stageMojo;

  @Before
  public void setup() throws IOException {
    Path sourceDirectory = tempFolder.newFolder("source").toPath();
    Mockito.when(stageMojo.getSourceDirectory()).thenReturn(sourceDirectory);
  }

  @Test
  public void testNewStager_noOpStager() throws MojoExecutionException {
    Mockito.when(stageMojo.isAppEngineCompatiblePackaging()).thenReturn(false);
    Stager stager = Stager.newStager(stageMojo);
    Assert.assertEquals(NoOpStager.class, stager.getClass());
  }

  @Test
  public void testNewStager_xml() throws MojoExecutionException {
    Mockito.when(stageMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(stageMojo.isAppEngineWebXmlBased()).thenReturn(true);
    Mockito.when(stageMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());

    Stager stager = Stager.newStager(stageMojo);
    Assert.assertEquals(AppEngineWebXmlStager.class, stager.getClass());
  }

  @Test
  public void testNewStager_yaml() throws MojoExecutionException {
    Mockito.when(stageMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(stageMojo.isAppEngineWebXmlBased()).thenReturn(false);
    Mockito.when(stageMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());

    Stager stager = Stager.newStager(stageMojo);
    Assert.assertEquals(AppYamlStager.class, stager.getClass());
  }

  @Test
  public void testNewStager_noArtifact() {
    Mockito.when(stageMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    try {
      Stager.newStager(stageMojo);
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(
          "\nCould not determine appengine environment, did you package your application?"
              + "\nRun 'mvn package appengine:stage'",
          ex.getMessage());
    }
  }
}
