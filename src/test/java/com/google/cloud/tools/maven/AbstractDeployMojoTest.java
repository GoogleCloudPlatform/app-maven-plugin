/*
 * Copyright 2018 Google LLC.
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

import java.lang.reflect.Field;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDeployMojoTest {

  private final AbstractDeployMojo testMojo =
      new AbstractDeployMojo() {
        @Override
        public void execute() {
          // do nothing;
        }
      };

  @Mock private Log mockLog;
  private Field projectField;

  @Before
  public void setUp() throws NoSuchFieldException {
    testMojo.setLog(mockLog);
    projectField = AbstractDeployMojo.class.getDeclaredField("project");
    projectField.setAccessible(true);
  }

  @Test
  public void testGetProjectId_onlyProject() throws IllegalAccessException {
    projectField.set(testMojo, "someProject");

    String projectId = testMojo.getProjectId();
    Assert.assertEquals("someProject", projectId);
    Mockito.verify(mockLog)
        .warn(
            "Configuring <project> is deprecated, use <projectId> to set your Google Cloud ProjectId");
    Mockito.verifyNoMoreInteractions(mockLog);
  }

  @Test
  public void testGetProjectId_onlyProjectId() throws IllegalAccessException {
    projectField.set(testMojo, "someProject");
    testMojo.setProjectId("someProjectId");

    try {
      testMojo.getProjectId();
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertEquals(
          "Configuring <project> and <projectId> is not allowed, please use only <projectId>",
          ex.getMessage());
    }
  }

  @Test
  public void testGetProjectId_bothProjectAndProjectId() {
    testMojo.setProjectId("someProjectId");
    Assert.assertEquals("someProjectId", testMojo.getProjectId());
    Mockito.verifyNoMoreInteractions(mockLog);
  }
}
