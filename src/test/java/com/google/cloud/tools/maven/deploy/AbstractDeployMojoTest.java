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
package com.google.cloud.tools.maven.deploy;

import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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

  @Before
  public void setUp() {
    testMojo.setLog(mockLog);
  }

  @Test
  public void testGetProjectId_onlyProject() {
    testMojo.project = "someProject";

    String projectId = testMojo.getProjectId();
    Assert.assertEquals("someProject", projectId);
    Mockito.verify(mockLog)
        .warn(
            "Configuring <project> is deprecated, use <projectId> to set your Google Cloud ProjectId");
    Mockito.verifyNoMoreInteractions(mockLog);
  }

  @Test
  public void testGetProjectId_bothProjectAndProjectId() {
    testMojo.project = "someProject";
    testMojo.projectId = "someProjectId";

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
  public void testGetProjectId_onlyProjectId() {
    testMojo.projectId = "someProjectId";
    Assert.assertEquals("someProjectId", testMojo.getProjectId());
    Mockito.verifyNoMoreInteractions(mockLog);
  }
}
