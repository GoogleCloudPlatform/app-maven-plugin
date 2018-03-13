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

package com.google.cloud.tools.maven;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

public abstract class AbstractDevServerTest {

  @Mock
  protected CloudSdkAppEngineFactory factoryMock;

  @Mock
  protected AppEngineDevServer devServerMock;

  @Mock
  protected MavenProject mavenProjectMock;

  protected void setUpAppEngineWebXml() throws IOException {
    TemporaryFolder tempFolder = new TemporaryFolder();
    tempFolder.create();
    File webInf = tempFolder.newFolder("build", "artifact", "WEB-INF");
    new File(webInf, "appengine-web.xml").createNewFile();
    when(mavenProjectMock.getBuild()).thenReturn(mock(Build.class));
    when(mavenProjectMock.getBuild().getDirectory())
        .thenReturn(webInf.getParentFile().getParentFile().getAbsolutePath());
    when(mavenProjectMock.getBuild().getFinalName()).thenReturn("artifact");
  }
}
