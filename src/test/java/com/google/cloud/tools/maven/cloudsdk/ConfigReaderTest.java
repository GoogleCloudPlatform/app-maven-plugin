package com.google.cloud.tools.maven.cloudsdk;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.appengine.operations.cloudsdk.serialization.CloudSdkConfig;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
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
public class ConfigReaderTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock Gcloud gcloud;
  @InjectMocks private ConfigReader testReader;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    Mockito.when(gcloud.getConfig()).thenReturn(mock(CloudSdkConfig.class));
  }

  @Test
  public void testGetProjectId_gcloudPass() throws Exception {
    Mockito.when(gcloud.getConfig().getProject()).thenReturn("some-project");

    Assert.assertEquals("some-project", testReader.getProjectId());
  }

  @Test
  @Parameters({"null", ""})
  public void testGetProjectId_gcloudFail(@Nullable String gcloudProject) throws Exception {
    Mockito.when(gcloud.getConfig().getProject()).thenReturn(gcloudProject);

    try {
      testReader.getProjectId();
      fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals("Project was not found in gcloud config", ex.getMessage());
    }
  }
}
