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
  public void deployAll() throws MojoExecutionException, MojoFailureException {
    stager.stage();
    deployMojo.deployables.clear();

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
      updateGcloudProperties();
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  /** Validates project/version configuration and pulls from appengine-web.xml if necessary */
  @VisibleForTesting
  @Override
  public void updateGcloudProperties() throws MojoExecutionException {
    if (deployMojo.project == null
        || deployMojo.project.trim().isEmpty()
        || deployMojo.project.equals(APPENGINE_CONFIG)) {
      throw new MojoExecutionException(
          "Deployment project must be defined or configured to read from system state\n"
              + "1. Set <project>my-project-name</project>\n"
              + "2. Set <project>"
              + GCLOUD_CONFIG
              + "</project> to use project from gcloud config.\n"
              + "3. Using <project>"
              + APPENGINE_CONFIG
              + "</project> is not allowed for flexible environment projects");
    } else if (deployMojo.project.equals(GCLOUD_CONFIG)) {
      deployMojo.project = null;
    }

    if (deployMojo.version == null
        || deployMojo.version.trim().isEmpty()
        || deployMojo.version.equals(APPENGINE_CONFIG)) {
      throw new MojoExecutionException(
          "Deployment version must be defined or configured to read from system state\n"
              + "1. Set <version>my-version</version>\n"
              + "2. Set <version>"
              + GCLOUD_CONFIG
              + "</version> to use version from gcloud config.\n"
              + "3. Using <version>"
              + APPENGINE_CONFIG
              + "</version> is not allowed for flexible environment projects");
    } else if (deployMojo.version.equals(GCLOUD_CONFIG)) {
      deployMojo.version = null;
    }
  }
}
