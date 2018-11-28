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

package com.google.cloud.tools.maven.stage;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineArchiveStaging;
import com.google.cloud.tools.appengine.api.deploy.StageArchiveConfiguration;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.stage.AppYamlStager.ConfigBuilder;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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

@RunWith(JUnitParamsRunner.class)
public class AppYamlStagerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private CloudSdkAppEngineFactory appengineFactory;
  @Mock private MavenProject mavenProject;
  @Mock private AppEngineArchiveStaging archiveStaging;
  @Mock private Log logMock;
  @Mock private StageArchiveConfiguration stageArchiveConfiguration;

  @Mock private ConfigBuilder configBuilder;
  @Mock private AbstractStageMojo stageMojo;
  @InjectMocks private AppYamlStager testStager;

  @Before
  public void configureStageMojo() {
    MockitoAnnotations.initMocks(this);
    when(stageMojo.getMavenProject()).thenReturn(mavenProject);
    when(stageMojo.getLog()).thenReturn(logMock);
    when(stageMojo.getAppEngineFactory()).thenReturn(appengineFactory);
    when(appengineFactory.appYamlStaging()).thenReturn(archiveStaging);
    when(configBuilder.buildConfiguration()).thenReturn(stageArchiveConfiguration);
    when(stageArchiveConfiguration.getStagingDirectory()).thenReturn(tempFolder.getRoot().toPath());
  }

  @Test
  @Parameters({"jar", "war"})
  public void testStage(String packaging) throws Exception {

    // wire up
    when(mavenProject.getPackaging()).thenReturn(packaging);

    // invoke
    testStager.stage();

    // verify
    verify(appengineFactory).appYamlStaging();
    verify(archiveStaging).stageArchive(stageArchiveConfiguration);
    verify(logMock).info("Detected App Engine app.yaml based application.");
  }

  @Test
  @Parameters({"pom", "ear", "rar", "par", "ejb", "maven-plugin", "eclipse-plugin"})
  public void testRun_packagingIsNotJarOrWar(String packaging)
      throws MojoFailureException, MojoExecutionException {

    // wire up
    when(stageMojo.getMavenProject().getPackaging()).thenReturn(packaging);

    testStager.stage();
    verify(logMock).info("Stage/deploy is only executed for war and jar modules.");
    verifyNoMoreInteractions(appengineFactory);
  }
}