package com.google.cloud.tools.maven.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.maven.AbstractDeployMojo;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SingleYamlStandardDeployTestHelper<M extends AbstractDeployMojo>
    extends ExternalResource {

  @Mock private AppEngineStandardStaging standardStagingMock;

  @Mock private AppEngineDeployment deploymentMock;

  @Mock private CloudSdkAppEngineFactory factoryMock;

  @Mock private MavenProject mavenProject;

  @Mock private File artifact;

  @InjectMocks protected M mojo;

  private TemporaryFolder temporaryFolder;

  public SingleYamlStandardDeployTestHelper(M mojo, TemporaryFolder temporaryFolder) {
    this.mojo = mojo;
    this.temporaryFolder = temporaryFolder;
  }

  @Override
  public void before() throws IOException {
    mojo.setStagingDirectory(temporaryFolder.newFolder("staging"));
    mojo.setSourceDirectory(temporaryFolder.newFolder("source"));
    mojo.setProjectId("test-project");
    mojo.setVersion("some-version");
    MockitoAnnotations.initMocks(this);

    // create appengine-web.xml to mark it as standard environment
    File webInfDirectory = mojo.getSourceDirectory().toPath().resolve("WEB-INF").toFile();
    Assert.assertTrue(webInfDirectory.mkdir());
    File appengineWebXml = webInfDirectory.toPath().resolve("appengine-web.xml").toFile();
    Assert.assertTrue(appengineWebXml.createNewFile());
    Files.asCharSink(appengineWebXml, Charsets.UTF_8)
        .write("<appengine-web-app></appengine-web-app>");
    when(artifact.exists()).thenReturn(true);
    when(factoryMock.standardStaging()).thenReturn(standardStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);
    when(mavenProject.getProperties()).thenReturn(new Properties());
    when(mavenProject.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
  }

  @Override
  public void after() {
    try {
      verify(standardStagingMock).stageStandard(mojo);
    } catch (AppEngineException ex) {
      Assert.fail(ex.getMessage());
    }
    assertEquals(
        Paths.get(
                temporaryFolder.getRoot().getAbsolutePath(),
                "staging",
                "WEB-INF",
                "appengine-generated")
            .toString(),
        mojo.getAppEngineDirectory().getAbsolutePath());
  }

  public AppEngineDeployment getDeploymentMock() {
    return deploymentMock;
  }
}
