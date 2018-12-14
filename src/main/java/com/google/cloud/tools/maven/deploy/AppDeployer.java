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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployProjectConfigurationConfiguration;
import com.google.cloud.tools.maven.config.ConfigProcessor;
import com.google.cloud.tools.maven.stage.Stager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

public class AppDeployer implements Deployer {

  @VisibleForTesting final Stager stager;
  @VisibleForTesting final ConfigProcessor configProcessor;
  private final AbstractDeployMojo deployMojo;
  private final ConfigBuilder configBuilder;

  @VisibleForTesting
  AppDeployer(
      AbstractDeployMojo deployMojo,
      Stager stager,
      ConfigProcessor configProcessor,
      ConfigBuilder configBuilder) {
    this.deployMojo = deployMojo;
    this.stager = stager;
    this.configProcessor = configProcessor;
    this.configBuilder = configBuilder;
  }

  /** Deploy a single application (and no project configuration). */
  public void deploy() throws MojoExecutionException {
    stager.stage();

    DeployConfiguration config =
        configBuilder.buildDeployConfiguration(ImmutableList.of(deployMojo.getStagingDirectory()));

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(config);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("App Engine application deployment failed", ex);
    }
  }

  /** Deploy a single application and any found yaml configuration files. */
  public void deployAll() throws MojoExecutionException {
    stager.stage();
    ImmutableList.Builder<Path> computedDeployables = ImmutableList.builder();

    // Look for app.yaml
    Path appYaml = deployMojo.getStagingDirectory().resolve("app.yaml");
    if (!Files.exists(appYaml)) {
      throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
    }
    deployMojo.getLog().info("deployAll: Preparing to deploy app.yaml");
    computedDeployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path appengineConfigPath = configProcessor.processAppEngineDirectory(deployMojo);
    for (String yamlName : configYamls) {
      Path yaml = appengineConfigPath.resolve(yamlName);
      if (Files.exists(yaml)) {
        deployMojo.getLog().info("deployAll: Preparing to deploy " + yamlName);
        computedDeployables.add(yaml);
      }
    }

    DeployConfiguration config =
        configBuilder.buildDeployConfiguration(computedDeployables.build());

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(config);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  /** Deploy only cron.yaml. */
  public void deployCron() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo
          .getAppEngineFactory()
          .deployment()
          .deployCron(configBuilder.buildDeployProjectConfigurationConfiguration());
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  /** Deploy only dispatch.yaml. */
  public void deployDispatch() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo
          .getAppEngineFactory()
          .deployment()
          .deployDispatch(configBuilder.buildDeployProjectConfigurationConfiguration());
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  /** Deploy only dos.yaml. */
  public void deployDos() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo
          .getAppEngineFactory()
          .deployment()
          .deployDos(configBuilder.buildDeployProjectConfigurationConfiguration());
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  /** Deploy only index.yaml. */
  public void deployIndex() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo
          .getAppEngineFactory()
          .deployment()
          .deployIndex(configBuilder.buildDeployProjectConfigurationConfiguration());
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  /** Deploy only queue.yaml. */
  public void deployQueue() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo
          .getAppEngineFactory()
          .deployment()
          .deployQueue(configBuilder.buildDeployProjectConfigurationConfiguration());
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  static class ConfigBuilder {

    private final AbstractDeployMojo deployMojo;
    private final ConfigProcessor configProcessor;

    ConfigBuilder(AbstractDeployMojo deployMojo, ConfigProcessor configProcessor) {
      this.deployMojo = deployMojo;
      this.configProcessor = configProcessor;
    }

    DeployConfiguration buildDeployConfiguration(List<Path> deployables) {
      return DeployConfiguration.builder(deployables)
          .bucket(deployMojo.getBucket())
          .imageUrl(deployMojo.getImageUrl())
          .projectId(configProcessor.processProjectId(deployMojo.getProjectId()))
          .promote(deployMojo.getPromote())
          .server(deployMojo.getServer())
          .stopPreviousVersion(deployMojo.getStopPreviousVersion())
          .version(configProcessor.processVersion(deployMojo.getVersion()))
          .build();
    }

    DeployProjectConfigurationConfiguration buildDeployProjectConfigurationConfiguration() {
      return DeployProjectConfigurationConfiguration.builder(
              configProcessor.processAppEngineDirectory(deployMojo))
          .projectId(configProcessor.processProjectId(deployMojo.getProjectId()))
          .server(deployMojo.getServer())
          .build();
    }
  }
}
