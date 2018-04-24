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
import java.io.File;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class AppEngineFlexibleDeployer implements AppEngineDeployer {

  private AbstractDeployMojo deployMojo;
  private AppEngineStager stager;

  AppEngineFlexibleDeployer(AbstractDeployMojo deployMojo) {
    this(deployMojo, AppEngineStager.Factory.newStager(deployMojo));
  }

  @VisibleForTesting
  AppEngineFlexibleDeployer(AbstractDeployMojo deployMojo, AppEngineStager stager) {
    this.deployMojo = deployMojo;
    this.stager = stager;
  }

  @Override
  public void deploy() throws MojoFailureException, MojoExecutionException {
    stager.stage();
    if (deployMojo.deployables.isEmpty()) {
      deployMojo.deployables.add(deployMojo.stagingDirectory);
    }

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployAll() throws MojoExecutionException, MojoFailureException {
    stager.stage();
    if (!deployMojo.deployables.isEmpty()) {
      deployMojo.getLog().warn("Ignoring configured deployables for deployAll.");
      deployMojo.deployables.clear();
    }

    // Look for app.yaml
    File appYaml = deployMojo.stagingDirectory.toPath().resolve("app.yaml").toFile();
    if (!appYaml.exists()) {
      appYaml = deployMojo.appEngineDirectory.toPath().resolve("app.yaml").toFile();
      if (!appYaml.exists()) {
        throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
      }
    }
    deployMojo.getLog().info("deployAll: Preparing to deploy app.yaml");
    deployMojo.deployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path configPath = deployMojo.appEngineDirectory.toPath();
    for (String yamlName : configYamls) {
      File yaml = configPath.resolve(yamlName).toFile();
      if (yaml.exists()) {
        deployMojo.getLog().info("deployAll: Preparing to deploy " + yamlName);
        deployMojo.deployables.add(yaml);
      }
    }

    try {
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployCron() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDispatch() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDos() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployIndex() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployQueue() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }
}
