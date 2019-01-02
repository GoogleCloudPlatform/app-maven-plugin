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

package com.google.cloud.tools.maven.deploy;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.DeployConfiguration;
import com.google.cloud.tools.appengine.configuration.DeployProjectConfigurationConfiguration;
import com.google.cloud.tools.appengine.operations.Deployment;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory;
import com.google.cloud.tools.maven.config.AppEngineWebXmlConfigProcessor;
import com.google.cloud.tools.maven.config.AppYamlConfigProcessor;
import com.google.cloud.tools.maven.config.ConfigProcessor;
import com.google.cloud.tools.maven.deploy.AppDeployer.ConfigBuilder;
import com.google.cloud.tools.maven.stage.AppEngineWebXmlStager;
import com.google.cloud.tools.maven.stage.AppYamlStager;
import com.google.cloud.tools.maven.stage.Stager;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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
public class AppDeployerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private ConfigBuilder configBuilder;
  @Mock private ConfigProcessor configProcessor;
  @Mock private Stager stager;
  @Mock private AbstractDeployMojo deployMojo;

  private Path stagingDirectory;
  private Path yamlConfigDirectory;
  @Mock private CloudSdkAppEngineFactory appEngineFactory;
  @Mock private Deployment appEngineDeployment;
  @Mock private DeployConfiguration deployConfiguration;
  @Mock private DeployProjectConfigurationConfiguration deployProjectConfigurationConfiguration;
  @Mock private Log mockLog;

  @InjectMocks private AppDeployer testDeployer;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    stagingDirectory = tempFolder.newFolder("staging").toPath();
    yamlConfigDirectory = tempFolder.newFolder("yaml-config").toPath();

    Mockito.when(deployMojo.getStagingDirectory()).thenReturn(stagingDirectory);
    Mockito.when(deployMojo.getAppEngineFactory()).thenReturn(appEngineFactory);
    Mockito.when(appEngineFactory.deployment()).thenReturn(appEngineDeployment);
    Mockito.when(configBuilder.buildDeployConfiguration(Mockito.any()))
        .thenReturn(deployConfiguration);
    Mockito.when(configBuilder.buildDeployProjectConfigurationConfiguration())
        .thenReturn(deployProjectConfigurationConfiguration);
    Mockito.when(configProcessor.processAppEngineDirectory(deployMojo))
        .thenReturn(yamlConfigDirectory);
    Mockito.when(deployMojo.getLog()).thenReturn(mockLog);
  }

  @Test
  public void testNewDeployer_appengineWebXml() throws MojoExecutionException {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(deployMojo.isAppEngineWebXmlBased()).thenReturn(true);
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());
    Mockito.when(deployMojo.getSourceDirectory()).thenReturn(tempFolder.getRoot().toPath());

    AppDeployer deployer = (AppDeployer) new Deployer.Factory().newDeployer(deployMojo);
    Mockito.verify(deployMojo).getAppEngineWebXml();
    Assert.assertEquals(AppEngineWebXmlConfigProcessor.class, deployer.configProcessor.getClass());
    Assert.assertEquals(AppEngineWebXmlStager.class, deployer.stager.getClass());
  }

  @Test
  public void testNewDeployer_appYaml() throws MojoExecutionException {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    Mockito.when(deployMojo.getArtifact()).thenReturn(tempFolder.getRoot().toPath());

    AppDeployer deployer = (AppDeployer) new Deployer.Factory().newDeployer(deployMojo);
    Mockito.verify(deployMojo, times(0)).getAppEngineWebXml();
    Assert.assertEquals(AppYamlConfigProcessor.class, deployer.configProcessor.getClass());
    Assert.assertEquals(AppYamlStager.class, deployer.stager.getClass());
  }

  @Test
  public void testNewDeployer_noArtifact() {
    Mockito.when(deployMojo.isAppEngineCompatiblePackaging()).thenReturn(true);
    try {
      new Deployer.Factory().newDeployer(deployMojo);
      fail();
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

  @Test
  public void testDeploy() throws MojoExecutionException, AppEngineException {
    testDeployer.deploy();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployConfiguration(ImmutableList.of(stagingDirectory));
    Mockito.verify(appEngineDeployment).deploy(deployConfiguration);
  }

  private List<Path> createStagedYamls(String... names) throws IOException {
    List<Path> createdFiles = new ArrayList<>();
    for (String name : names) {
      createdFiles.add(Files.createFile(yamlConfigDirectory.resolve(name + ".yaml")));
    }
    return createdFiles;
  }

  private Path createAppYaml() throws IOException {
    return Files.createFile(stagingDirectory.resolve("app.yaml"));
  }

  @Test
  public void testDeployAll_allYamls()
      throws MojoExecutionException, AppEngineException, IOException {
    List<Path> files =
        ImmutableList.<Path>builder()
            .add(createAppYaml())
            .addAll(createStagedYamls("cron", "dispatch", "dos", "index", "queue"))
            .build();
    // also create a file we'll ignore
    Files.createFile(yamlConfigDirectory.resolve("ignored"));

    testDeployer.deployAll();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployConfiguration(Mockito.eq(files));
    Mockito.verify(appEngineDeployment).deploy(deployConfiguration);
  }

  @Test
  public void testDeployAll_someYamls()
      throws MojoExecutionException, AppEngineException, IOException {
    List<Path> files =
        ImmutableList.<Path>builder()
            .add(createAppYaml())
            .addAll(createStagedYamls("dispatch", "dos", "queue"))
            .build();
    // also create a file we'll ignore
    Files.createFile(yamlConfigDirectory.resolve("ignored"));

    testDeployer.deployAll();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployConfiguration(files);
    Mockito.verify(appEngineDeployment).deploy(deployConfiguration);
  }

  @Test
  public void testDeployAll_noAppYaml() throws IOException {
    createStagedYamls("dos", "cron");

    try {
      testDeployer.deployAll();
      fail();
    } catch (MojoExecutionException ex) {
      Assert.assertEquals("Failed to deploy all: could not find app.yaml.", ex.getMessage());
    }
  }

  @Test
  public void testDeployCron() throws MojoExecutionException, AppEngineException {
    testDeployer.deployCron();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployProjectConfigurationConfiguration();
    Mockito.verify(appEngineDeployment).deployCron(deployProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployDos() throws MojoExecutionException, AppEngineException {
    testDeployer.deployDos();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployProjectConfigurationConfiguration();
    Mockito.verify(appEngineDeployment).deployDos(deployProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployDispatch() throws MojoExecutionException, AppEngineException {
    testDeployer.deployDispatch();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployProjectConfigurationConfiguration();
    Mockito.verify(appEngineDeployment).deployDispatch(deployProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployIndex() throws MojoExecutionException, AppEngineException {
    testDeployer.deployIndex();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployProjectConfigurationConfiguration();
    Mockito.verify(appEngineDeployment).deployIndex(deployProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployQueue() throws MojoExecutionException, AppEngineException {
    testDeployer.deployQueue();
    Mockito.verify(stager).stage();
    Mockito.verify(configBuilder).buildDeployProjectConfigurationConfiguration();
    Mockito.verify(appEngineDeployment).deployQueue(deployProjectConfigurationConfiguration);
  }

  @Test
  public void testBuildDeployConfiguration() {
    AbstractDeployMojo deployMojo = Mockito.mock(AbstractDeployMojo.class);
    Mockito.when(deployMojo.getBucket()).thenReturn("testBucket");
    Mockito.when(deployMojo.getImageUrl()).thenReturn("testImageUrl");
    Mockito.when(deployMojo.getProjectId()).thenReturn("testProjectId");
    Mockito.when(deployMojo.getPromote()).thenReturn(false);
    Mockito.when(deployMojo.getStopPreviousVersion()).thenReturn(false);
    Mockito.when(deployMojo.getServer()).thenReturn("testServer");
    Mockito.when(deployMojo.getVersion()).thenReturn("testVersion");

    ConfigProcessor configProcessor = Mockito.mock(ConfigProcessor.class);
    Mockito.when(configProcessor.processProjectId("testProjectId"))
        .thenReturn("processedTestProjectId");
    Mockito.when(configProcessor.processVersion("testVersion")).thenReturn("processedTestVersion");

    List<Path> deployables = ImmutableList.of(Paths.get("some/path"), Paths.get("some/other/path"));
    DeployConfiguration deployConfig =
        new ConfigBuilder(deployMojo, configProcessor).buildDeployConfiguration(deployables);

    Assert.assertEquals(deployables, deployConfig.getDeployables());
    Assert.assertEquals("testBucket", deployConfig.getBucket());
    Assert.assertEquals("testImageUrl", deployConfig.getImageUrl());
    Assert.assertEquals("processedTestProjectId", deployConfig.getProjectId());
    Assert.assertEquals(Boolean.FALSE, deployConfig.getPromote());
    Assert.assertEquals(Boolean.FALSE, deployConfig.getStopPreviousVersion());
    Assert.assertEquals("testServer", deployConfig.getServer());
    Assert.assertEquals("processedTestVersion", deployConfig.getVersion());
  }
}
