/*
 * Copyright (C) 2016 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class RunMojoTest extends AbstractDevServerTest {

  @InjectMocks
  private RunMojo runMojo;

  @Test
  public void testRun() throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    setUpAppEngineWebXml();
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    // invoke
    runMojo.execute();

    // verify
    verify(devServerMock).run(runMojo);
  }

  @Test
  public void testRunFlexible() throws MojoFailureException, MojoExecutionException, IOException {
    // wire up
    when(factoryMock.devServerRunSync()).thenReturn(devServerMock);

    // invoke
    try {
      runMojo.execute();
      fail("Expected exception");
    } catch (MojoExecutionException e) {
      // verify
      assertEquals("Dev App Server does not support App Engine Flexible Environment applications.",
          e.getMessage());
    }
  }
}
