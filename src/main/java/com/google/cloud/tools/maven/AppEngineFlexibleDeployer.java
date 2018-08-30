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

package com.google.cloud.tools.maven;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;

public class AppEngineFlexibleDeployer implements AppEngineDeployer {

  private AbstractDeployMojo deployMojo;
  private AppEngineFlexibleStager stager;

  AppEngineFlexibleDeployer(AbstractDeployMojo deployMojo) {
    this(deployMojo, new AppEngineFlexibleStager(deployMojo));
  }

  @VisibleForTesting
  AppEngineFlexibleDeployer(AbstractDeployMojo deployMojo, AppEngineFlexibleStager stager) {
    this.deployMojo = deployMojo;
    this.stager = stager;

    stager.overrideAppEngineDirectory();
    setDeploymentProjectAndVersion();
  }

  @Override
  public void deploy() throws MojoExecutionException {
    stager.stage();
    deployMojo.setDeployables(ImmutableList.of(deployMojo.getStagingDirectory()));

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Flexible application deployment failed", ex);
    }
  }

  @Override
  public void deployAll() throws MojoExecutionException {
    stager.stage();
    ImmutableList.Builder<File> flexDeployables = ImmutableList.builder();

    // Look for app.yaml
    File appYaml = deployMojo.getStagingDirectory().toPath().resolve("app.yaml").toFile();
    if (!appYaml.exists()) {
      appYaml = deployMojo.getAppEngineDirectory().toPath().resolve("app.yaml").toFile();
      if (!appYaml.exists()) {
        throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
      }
    }
    deployMojo.getLog().info("deployAll: Preparing to deploy app.yaml");
    flexDeployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path configPath = deployMojo.getAppEngineDirectory().toPath();
    for (String yamlName : configYamls) {
      File yaml = configPath.resolve(yamlName).toFile();
      if (yaml.exists()) {
        deployMojo.getLog().info("deployAll: Preparing to deploy " + yamlName);
        flexDeployables.add(yaml);
      }
    }

    deployMojo.setDeployables(flexDeployables.build());

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployCron() throws MojoExecutionException {
    try {
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployDispatch() throws MojoExecutionException {
    try {
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployDos() throws MojoExecutionException {
    try {
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployIndex() throws MojoExecutionException {
    try {
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployQueue() throws MojoExecutionException {
    try {
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @VisibleForTesting
  private void setDeploymentProjectAndVersion() {
    String project = deployMojo.getProjectId();
    if (project == null || project.trim().isEmpty() || project.equals(APPENGINE_CONFIG)) {
      throw new IllegalArgumentException(
          "Deployment projectId must be defined or configured to read from system state\n"
              + "1. Set <deploy.projectId>my-project-id</deploy.projectId>\n"
              + "2. Set <deploy.projectId>"
              + GCLOUD_CONFIG
              + "</deploy.projectId> to use project from gcloud config.\n"
              + "3. Using <deploy.projectId>"
              + APPENGINE_CONFIG
              + "</deploy.projectId> is not allowed for flexible environment projects");
    } else if (project.equals(GCLOUD_CONFIG)) {
      deployMojo.setProjectId(null);
    }

    String version = deployMojo.getVersion();
    if (version == null || version.trim().isEmpty() || version.equals(APPENGINE_CONFIG)) {
      throw new IllegalArgumentException(
          "Deployment version must be defined or configured to read from system state\n"
              + "1. Set <deploy.version>my-version</deploy.version>\n"
              + "2. Set <deploy.version>"
              + GCLOUD_CONFIG
              + "</deploy.version> to use version from gcloud config.\n"
              + "3. Using <deploy.version>"
              + APPENGINE_CONFIG
              + "</deploy.version> is not allowed for flexible environment projects");
    } else if (version.equals(GCLOUD_CONFIG)) {
      deployMojo.setVersion(null);
    }
  }
}
