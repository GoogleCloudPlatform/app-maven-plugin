package com.google.cloud.tools.maven.util;

import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.maven.AbstractDeployMojo;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SingleYamlFlexibleDeployTestHelper<M extends AbstractDeployMojo>
    extends ExternalResource {

  @Mock private AppEngineFlexibleStaging flexibleStagingMock;

  @Mock private AppEngineDeployment deploymentMock;

  @Mock private CloudSdkAppEngineFactory factoryMock;

  @Mock private MavenProject mavenProject;

  @Mock private File artifact;

  @InjectMocks protected M mojo;

  private TemporaryFolder temporaryFolder;

  public SingleYamlFlexibleDeployTestHelper(M mojo, TemporaryFolder temporaryFolder) {
    this.mojo = mojo;
    this.temporaryFolder = temporaryFolder;
  }

  @Override
  public void before() throws IOException {
    mojo.setStagingDirectory(temporaryFolder.newFolder("staging"));
    mojo.setSourceDirectory(temporaryFolder.newFolder("source"));
    mojo.setProjectId("some-project");
    mojo.setVersion("some-version");
    MockitoAnnotations.initMocks(this);

    when(artifact.exists()).thenReturn(true);
    when(mavenProject.getProperties()).thenReturn(new Properties());
    when(mavenProject.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
    when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    when(factoryMock.deployment()).thenReturn(deploymentMock);
  }

  public AppEngineDeployment getDeploymentMock() {
    return deploymentMock;
  }
}
