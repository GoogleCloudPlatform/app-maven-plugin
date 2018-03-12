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

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class StageMojoTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @Mock
  private MavenProject mavenProject;

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
    MockitoAnnotations.initMocks(this);
    stageMojo.stagingDirectory = tempFolder.newFolder("staging");
    stageMojo.sourceDirectory = tempFolder.newFolder("source");
    when(mavenProject.getProperties()).thenReturn(new Properties());
    when(mavenProject.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
  }

  @Test
  @Parameters({"jar", "war"})
  public void testStandardStaging(String packaging) throws Exception {

    // wire up
    when(stageMojo.mavenProject.getPackaging()).thenReturn(packaging);
    when(factoryMock.standardStaging()).thenReturn(standardStagingMock);

    // create appengine-web.xml to mark it as standard environment
    File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appengineWebXml.createNewFile();
    Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);

    // invoke
    stageMojo.execute();

    // verify
    verify(standardStagingMock).stageStandard(stageMojo);
    verify(logMock).info(contains("standard"));
  }

  @Test
  @Parameters({"jar", "war"})
  public void testFlexibleStaging(String packaging) throws Exception {

    // wire up
    when(stageMojo.mavenProject.getPackaging()).thenReturn(packaging);
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);

    // invoke
    stageMojo.execute();

    // verify
    verify(flexibleStagingMock).stageFlexible(stageMojo);
    verify(logMock).info(contains("flexible"));
  }

  @Test
  @Parameters
  public void testRun_packagingIsNotJarOrWar(String packaging)
      throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    stageMojo.stagingDirectory = mock(File.class);
    when(stageMojo.mavenProject.getPackaging()).thenReturn(packaging);

    stageMojo.execute();
    verify(stageMojo.stagingDirectory, never()).exists();
  }

  @SuppressWarnings("unused") // used for testRun_packagingIsNotJarOrWar()
  private Object[][] parametersForTestRun_packagingIsNotJarOrWar(){
    return new Object[][]{
        new Object[]{ null },
        new Object[]{ "pom" },
        new Object[]{ "ear" },
        new Object[]{ "rar" },
        new Object[]{ "par" },
        new Object[]{ "ejb" },
        new Object[]{ "maven-plugin" },
        new Object[]{ "eclipse-plugin" }
    };
  }
}
