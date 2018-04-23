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
import java.io.File;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class AppEngineFlexibleDeployer implements AppEngineDeployer {

  @Override
  public void stage(StageMojo stageMojo) throws MojoExecutionException, MojoFailureException {
    if (!"war".equals(stageMojo.getPackaging()) && !"jar".equals(stageMojo.getPackaging())) {
      // https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/85
      stageMojo.getLog().info("Stage/deploy is only executed for war and jar modules.");
      return;
    }

    stageMojo.clearStagingDirectory();
    configureAppEngineDirectory(stageMojo);

    stageMojo.getLog().info("Staging the application to: " + stageMojo.stagingDirectory);
    stageMojo.getLog().info("Detected App Engine flexible environment application.");

    try {
      stageMojo.getAppEngineFactory().flexibleStaging().stageFlexible(stageMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void configureAppEngineDirectory(StageMojo stageMojo) {
    stageMojo.appEngineDirectory =
        stageMojo
            .mavenProject
            .getBasedir()
            .toPath()
            .resolve("src")
            .resolve("main")
            .resolve("appengine")
            .toFile();
  }

  @Override
  public void deploy(AbstractDeployMojo deployMojo) {
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
  public void deployAll(AbstractDeployMojo deployMojo) throws MojoExecutionException {
    if (!deployMojo.deployables.isEmpty()) {
      deployMojo.getLog().warn("Ignoring configured deployables for deployAll.");
      deployMojo.deployables.clear();
    }

    // Look for app.yaml
    File appYaml = deployMojo.stagingDirectory.toPath().resolve("app.yaml").toFile();
    if (!appYaml.exists()) {
      appYaml = deployMojo.appEngineDirectory.toPath().resolve("app.yaml").toFile();
    }
    if (!appYaml.exists()) {
      throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
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
  public void deployCron(AbstractDeployMojo deployMojo) {
    try {
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDispatch(AbstractDeployMojo deployMojo) {
    try {
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDos(AbstractDeployMojo deployMojo) {
    try {
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployIndex(AbstractDeployMojo deployMojo) {
    try {
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployQueue(AbstractDeployMojo deployMojo) {
    try {
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }
}
