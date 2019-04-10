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

package com.google.cloud.tools.maven.run;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunAsyncMojoTest {

  @Mock private Runner.Factory factory;
  @Mock private Runner runner;

  @InjectMocks private RunAsyncMojo testMojo;

  @Before
  public void setUp() throws MojoExecutionException {
    Mockito.when(factory.newRunner(testMojo)).thenReturn(runner);
  }

  @Test
  public void testExecute_smokeTest() throws MojoExecutionException {
    testMojo.startSuccessTimeout = 34;
    testMojo.execute();
    Mockito.verify(runner).runAsync(34);
  }

  @Test
  public void testExecute_skipTest() throws MojoExecutionException {
    testMojo.setSkip(true);
    testMojo.execute();
    Mockito.verifyNoMoreInteractions(runner);
  }
}
