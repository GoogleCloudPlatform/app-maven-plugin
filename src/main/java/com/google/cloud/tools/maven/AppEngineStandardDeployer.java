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

public class AppEngineStandardDeployer implements AppEngineDeployer {

  private AbstractDeployMojo deployMojo;
  private AppEngineStandardStager stager;

  AppEngineStandardDeployer(AbstractDeployMojo deployMojo) {
    this(deployMojo, new AppEngineStandardStager(deployMojo));
  }

  @VisibleForTesting
  AppEngineStandardDeployer(AbstractDeployMojo deployMojo, AppEngineStandardStager stager) {
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
      throw new MojoExecutionException("Standard application deployment failed", ex);
    }
  }

  @Override
  public void deployAll() throws MojoExecutionException {
    stager.stage();
    ImmutableList.Builder<File> standardDeployables = ImmutableList.builder();

    // Look for app.yaml
    File appYaml = deployMojo.getStagingDirectory().toPath().resolve("app.yaml").toFile();
    if (!appYaml.exists()) {
      throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
    }
    deployMojo.getLog().info("deployAll: Preparing to deploy app.yaml");
    standardDeployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path configPath =
        deployMojo.getStagingDirectory().toPath().resolve("WEB-INF").resolve("appengine-generated");
    for (String yamlName : configYamls) {
      File yaml = configPath.resolve(yamlName).toFile();
      if (yaml.exists()) {
        deployMojo.getLog().info("deployAll: Preparing to deploy " + yamlName);
        standardDeployables.add(yaml);
      }
    }

    deployMojo.setDeployables(standardDeployables.build());

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployCron() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployDispatch() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployDos() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployIndex() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @Override
  public void deployQueue() throws MojoExecutionException {
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to deploy", ex);
    }
  }

  @VisibleForTesting
  static final String PROJECT_ERROR =
      "Deployment projectId must be configured on the appengine-maven-plugin using"
          + " <deploy.projectId> or by setting the system property 'app.deploy.projectId'\n"
          + "1. Set projectId = my-project-id\n"
          + "2. Set projectId = "
          + APPENGINE_CONFIG
          + " to use <application> from appengine-web.xml\n"
          + "3. Set projectId = "
          + GCLOUD_CONFIG
          + " to use project from your gcloud configuration";

  @VisibleForTesting
  static final String VERSION_ERROR =
      "Deployment version must be configured on the appengine-maven-plugin using"
          + " <deploy.version> or by setting the system property 'app.deploy.version'\n"
          + "1. Set version = my-version\n"
          + "2. Set version = "
          + APPENGINE_CONFIG
          + " to use <version> from appengine-web.xml"
          + "3. Set version = "
          + GCLOUD_CONFIG
          + " to have gcloud generate a version for you";

  @VisibleForTesting
  void setDeploymentProjectAndVersion() {
    File appengineWebXml =
        deployMojo
            .getSourceDirectory()
            .toPath()
            .resolve("WEB-INF")
            .resolve("appengine-web.xml")
            .toFile();

    String project = deployMojo.getProjectId();
    if (project == null || project.trim().isEmpty()) {
      throw new IllegalArgumentException(PROJECT_ERROR);
    } else if (project.equals(APPENGINE_CONFIG)) {
      deployMojo.setProjectId(ConfigReader.getProject(appengineWebXml));
    } else if (project.equals(GCLOUD_CONFIG)) {
      deployMojo.setProjectId(
          ConfigReader.getProject(deployMojo.getAppEngineFactory().getGcloud()));
    }

    String version = deployMojo.getVersion();
    if (version == null || version.trim().isEmpty()) {
      throw new IllegalArgumentException(VERSION_ERROR);
    } else if (version.equals(APPENGINE_CONFIG)) {
      deployMojo.setVersion(ConfigReader.getVersion(appengineWebXml));
    } else if (version.equals(GCLOUD_CONFIG)) {
      deployMojo.setVersion(null);
    }
  }
}
