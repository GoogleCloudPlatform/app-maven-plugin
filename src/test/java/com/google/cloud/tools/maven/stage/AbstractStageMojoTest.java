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

package com.google.cloud.tools.maven.stage;

import java.io.File;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnitParamsRunner.class)
public class AbstractStageMojoTest {

  @Rule public TemporaryFolder testDirectory = new TemporaryFolder();

  @Mock private MavenProject mavenProject;
  @Mock private File sourceDirectory;

  @InjectMocks
  public AbstractStageMojo testMojo =
      new AbstractStageMojo() {
        @Override
        public void execute() {
          // do nothing
        }
      };

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Parameters({"pom", "ear", "rar", "par", "ejb", "maven-plugin", "eclipse-plugin"})
  public void testIsAppEngineCompatiblePackaging_false(String packaging) {
    Mockito.when(mavenProject.getPackaging()).thenReturn(packaging);

    Assert.assertFalse(testMojo.isAppEngineCompatiblePackaging());
  }

  @Test
  @Parameters({"jar", "war"})
  public void testIsAppEngineCompatiblePackaging_true(String packaging) {
    Mockito.when(mavenProject.getPackaging()).thenReturn(packaging);

    Assert.assertTrue(testMojo.isAppEngineCompatiblePackaging());
  }

  @Test
  public void testIsAppEngineWebXmlBased_true() throws IOException {
    Mockito.when(sourceDirectory.toPath()).thenReturn(testDirectory.getRoot().toPath());
    testDirectory.newFolder("WEB-INF");
    testDirectory.newFile("WEB-INF/appengine-web.xml");

    Assert.assertTrue(testMojo.isAppEngineWebXmlBased());
  }

  @Test
  public void testIsAppEngineWebXmlBased_false() throws IOException {
    Mockito.when(sourceDirectory.toPath()).thenReturn(testDirectory.getRoot().toPath());
    testDirectory.newFolder("WEB-INF");

    Assert.assertFalse(testMojo.isAppEngineWebXmlBased());
  }
}
