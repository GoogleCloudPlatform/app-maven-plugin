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

  private AbstractDeployMojo testMojo =
      new AbstractDeployMojo() {
        @Override
        public void execute() {
          // do nothing;
        }
      };

  @Mock private Log mockLog;
  private final Field projectField;

  public AbstractDeployMojoTest() throws NoSuchFieldException {
    projectField = AbstractDeployMojo.class.getDeclaredField("project");
    projectField.setAccessible(true);
  }

  @Before
  public void setUp() {
    testMojo.setLog(mockLog);
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
