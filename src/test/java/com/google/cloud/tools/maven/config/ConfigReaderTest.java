package com.google.cloud.tools.maven.config;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(JUnitParamsRunner.class)
public class ConfigReaderTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private ConfigReader testReader = new ConfigReader();

  @Test
  public void testGetProjectId_gcloudPass() throws Exception {
    Gcloud gcloud = mock(Gcloud.class);
    Mockito.when(gcloud.getConfig()).thenReturn(mock(CloudSdkConfig.class));
    Mockito.when(gcloud.getConfig().getProject()).thenReturn("some-project");

    Assert.assertEquals("some-project", testReader.getProjectId(gcloud));
  }

  @Test
  @Parameters({"null", ""})
  public void testGetProjectId_gcloudFail(@Nullable String gcloudProject) throws Exception {
    Gcloud gcloud = mock(Gcloud.class);
    Mockito.when(gcloud.getConfig()).thenReturn(mock(CloudSdkConfig.class));
    Mockito.when(gcloud.getConfig().getProject()).thenReturn(gcloudProject);
    try {
      testReader.getProjectId(gcloud);
      fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals("Project was not found in gcloud config", ex.getMessage());
    }
  }

  private Path generateAppEngineWebXml(String application, String version) throws IOException {
    Path appengineWebXml = temporaryFolder.getRoot().toPath().resolve("appengine-web.xml");
    StringBuilder fileContents = new StringBuilder();
    fileContents.append("<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>");
    if (application != null) {
      fileContents.append("<application>").append(application).append("</application>");
    }
    if (version != null) {
      fileContents.append("<version>").append(version).append("</version>");
    }
    fileContents.append("</appengine-web-app>");
    return Files.write(appengineWebXml, fileContents.toString().getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @Parameters({"null", "some-version"})
  public void getProjectId_xmlPass(@Nullable String version) throws IOException {
    Assert.assertEquals(
        "some-application",
        testReader.getProjectId(generateAppEngineWebXml("some-application", version)));
  }

  @Test
  @Parameters({"null|null", "null|some-version", "|null", "|some-version"})
  public void getProjectId_xmlFail(@Nullable String xmlApplication, @Nullable String version)
      throws IOException {
    try {
      Assert.assertEquals(
          "some-application",
          testReader.getProjectId(generateAppEngineWebXml(xmlApplication, version)));
      fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals("<application> was not found in appengine-web.xml", ex.getMessage());
    }
  }

  @Test
  @Parameters({"null", "some-application"})
  public void getVersion_xmlPass(@Nullable String application) throws IOException {
    Assert.assertEquals(
        "some-version",
        testReader.getVersion(generateAppEngineWebXml(application, "some-version")));
  }

  @Test
  @Parameters({"null|null", "null|some-application", "|null", "|some-application"})
  public void getVersion_xmlFail(@Nullable String version, @Nullable String application) {
    try {
      Assert.assertEquals(
          "some-application", testReader.getVersion(generateAppEngineWebXml(application, version)));
      fail();
    } catch (Exception ex) {
      Assert.assertEquals("<version> was not found in appengine-web.xml", ex.getMessage());
    }
  }
}
