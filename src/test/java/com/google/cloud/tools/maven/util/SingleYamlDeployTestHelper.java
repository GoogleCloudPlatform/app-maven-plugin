package com.google.cloud.tools.maven.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.maven.AbstractSingleYamlDeployMojo;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

public class SingleYamlDeployTestHelper<M extends AbstractSingleYamlDeployMojo>
    extends ExternalResource {
  
  @Mock
  private AppEngineFlexibleStaging flexibleStagingMock;
  
  @Mock
  private AppEngineStandardStaging standardStagingMock;

  @Mock
  private AppEngineDeployment deploymentMock;

  @Mock
  private CloudSdkAppEngineFactory factoryMock;

  @InjectMocks
  protected M mojo;

  private boolean isStandard;

  private TemporaryFolder temporaryFolder;

  public SingleYamlDeployTestHelper(M mojo, TemporaryFolder temporaryFolder, boolean b) {
    this.mojo = mojo;
    this.temporaryFolder = temporaryFolder;
    this.isStandard = b;
  }

  @Override
  public void before() throws IOException {
    mojo.setStagingDirectory(temporaryFolder.newFolder("staging"));
    mojo.setSourceDirectory(temporaryFolder.newFolder("source"));
    MockitoAnnotations.initMocks(this);

    if (isStandard) {
      // create appengine-web.xml to mark it as standard environment
      File webInfDirectory = mojo.getSourceDirectory().toPath().resolve("WEB-INF").toFile();
      webInfDirectory.mkdir();
      File appengineWebXml = webInfDirectory.toPath().resolve("appengine-web.xml").toFile();
      appengineWebXml.createNewFile();
      Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);
      when(factoryMock.standardStaging()).thenReturn(standardStagingMock);
    } else {
      when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
    }
    when(factoryMock.deployment()).thenReturn(deploymentMock);
  }
  
  @Override
  public void after() {
    if (isStandard) {
      verify(standardStagingMock).stageStandard(mojo);
    } else {
      verify(flexibleStagingMock).stageFlexible(mojo);
    }
  }
    
  public AppEngineDeployment getDeploymentMock() {
    return deploymentMock;
  }

  public static <N extends AbstractSingleYamlDeployMojo>
      SingleYamlDeployTestHelper<N> forStandard(N mojo, TemporaryFolder temporaryFolder) {
        SingleYamlDeployTestHelper<N> testFixture =
            new SingleYamlDeployTestHelper<N>(mojo, temporaryFolder, true);
        return testFixture;
  }
  
  public static <N extends AbstractSingleYamlDeployMojo>
      SingleYamlDeployTestHelper<N> forFlex(N mojo, TemporaryFolder temporaryFolder) {
        SingleYamlDeployTestHelper<N> testFixture =
            new SingleYamlDeployTestHelper<N>(mojo, temporaryFolder, false);
        return testFixture;
  }
}