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

package com.google.cloud.tools.maven.genrepoinfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.GenRepoInfoFileConfiguration;
import com.google.cloud.tools.appengine.operations.GenRepoInfoFile;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import java.nio.file.Paths;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** {@link GenRepoInfoFileMojo} unit tests. */
@RunWith(MockitoJUnitRunner.class)
public class GenRepoInfoFileMojoTest {
  @Mock private CloudSdkAppEngineFactory factory;

  @Mock private GenRepoInfoFile genMock;

  @InjectMocks private GenRepoInfoFileMojo genMojo;

  @Before
  public void init() {
    genMojo.sourceDirectory = Paths.get("/a/b/c/source").toFile();
    genMojo.outputDirectory = Paths.get("/e/f/g/output").toFile();
    when(factory.genRepoInfoFile()).thenReturn(genMock);
  }

  @Test
  public void testExecute()
      throws MojoFailureException, MojoExecutionException, AppEngineException {
    genMojo.ignoreErrors = true;
    genMojo.execute();

    ArgumentCaptor<GenRepoInfoFileConfiguration> captor =
        ArgumentCaptor.forClass(GenRepoInfoFileConfiguration.class);
    verify(genMock).generate(captor.capture());
    Assert.assertEquals(genMojo.sourceDirectory.toPath(), captor.getValue().getSourceDirectory());
    Assert.assertEquals(genMojo.outputDirectory.toPath(), captor.getValue().getOutputDirectory());
  }

  @Test
  public void testExecute_noIgnoreErrors() throws MojoFailureException, AppEngineException {

    doThrow(new AppEngineException("Bad")).when(genMock).generate(any());

    try {
      genMojo.execute();
    } catch (MojoExecutionException ex) {
      // expected
    }

    verify(genMock).generate(any());
  }
}
