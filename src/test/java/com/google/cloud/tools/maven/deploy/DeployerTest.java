package com.google.cloud.tools.maven.deploy;

import static org.mockito.Mockito.times;

import com.google.cloud.tools.appengine.configuration.DeployConfiguration;
import com.google.cloud.tools.appengine.configuration.DeployProjectConfigurationConfiguration;
import com.google.cloud.tools.appengine.operations.Deployment;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.deploy.AppDeployer.ConfigBuilder;
import com.google.cloud.tools.maven.stage.AppEngineWebXmlStager;
import com.google.cloud.tools.maven.stage.AppYamlStager;
import com.google.cloud.tools.maven.stage.Stager;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeployerTest {

  @Rule public final TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private ConfigProcessor configProcessor;

  @Mock private ConfigBuilder configBuilder;
  @Mock private Stager stager;
  @Mock private AbstractDeployMojo deployMojo;

  private Path stagingDirectory;
  private Path yamlConfigDirectory;
  @Mock private CloudSdkAppEngineFactory appEngineFactory;
  @Mock private Deployment appEngineDeployment;
  @Mock private Path appengineDirectory;
  @Mock private DeployConfiguration deployConfiguration;
  @Mock private DeployProjectConfigurationConfiguration deployProjectConfigurationConfiguration;

  @InjectMocks private AppDeployer testDeployer;

  @Before
  public void setup() throws IOException {
    stagingDirectory = tempFolder.newFolder("staging").toPath();
    yamlConfigDirectory = tempFolder.newFolder("yaml-config").toPath();

    Mockito.when(deployMojo.getStagingDirectory()).thenReturn(stagingDirectory);
    Mockito.when(deployMojo.getAppEngineFactory()).thenReturn(appEngineFactory);
  }

  @Test
  public void testNewDeployer_appengineWebXml() throws MojoExecutionException {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(deployMojo.isAppEngineWebXmlBased()).thenReturn(true);
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());

    AppDeployer deployer = (AppDeployer) new Deployer.Factory().newDeployer(deployMojo);
    Assert.assertEquals(
        deployMojo.getStagingDirectory().resolve("WEB-INF").resolve("appengine-generated"),
        deployer.appengineDirectory);
    Assert.assertEquals(AppEngineWebXmlStager.class, deployer.stager.getClass());
  }

  @Test
  public void testNewDeployer_appYaml() throws MojoExecutionException, IOException {
    Path appengineDir = tempFolder.newFolder().toPath();
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());
    Mockito.when(deployMojo.getAppEngineDirectory()).thenReturn(appengineDir);

    AppDeployer deployer = (AppDeployer) new Deployer.Factory().newDeployer(deployMojo);
    Mockito.verify(deployMojo, times(0)).getAppEngineWebXml();
    Assert.assertEquals(appengineDir, deployer.appengineDirectory);
    Assert.assertEquals(AppYamlStager.class, deployer.stager.getClass());
  }

  @Test
  public void testNewDeployer_noArtifact() {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    try {
      new Deployer.Factory().newDeployer(deployMojo);
      Assert.fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals(
          "\nCould not determine appengine environment, did you package your application?"
              + "\nRun 'mvn package appengine:deploy'",
          ex.getMessage());
    }
  }

  @Test
  public void testNewDeployer_noOpDeployer() throws MojoExecutionException {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(false);
    Assert.assertEquals(
        NoOpDeployer.class, new Deployer.Factory().newDeployer(deployMojo).getClass());
  }
}
