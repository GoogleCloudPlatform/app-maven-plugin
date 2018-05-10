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

import com.google.cloud.tools.appengine.AppEngineDescriptor;
import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

public class AppEngineStandardDeployer implements AppEngineDeployer {

  private AbstractDeployMojo deployMojo;
  private AppEngineStager stager;

  AppEngineStandardDeployer(AbstractDeployMojo deployMojo) {
    this(deployMojo, AppEngineStager.Factory.newStager(deployMojo));
  }

  @VisibleForTesting
  AppEngineStandardDeployer(AbstractDeployMojo deployMojo, AppEngineStager stager) {
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
      throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
    }
    deployMojo.getLog().info("deployAll: Preparing to deploy app.yaml");
    deployMojo.deployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path configPath =
        deployMojo.stagingDirectory.toPath().resolve("WEB-INF").resolve("appengine-generated");
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
    File appengineWebXml =
        deployMojo
            .getSourceDirectory()
            .toPath()
            .resolve("WEB-INF")
            .resolve("appengine-web.xml")
            .toFile();

    if (deployMojo.project == null || deployMojo.project.trim().isEmpty()) {
      throw new MojoExecutionException(
          "Deployment project must be defined or configured to read from system state\n"
              + "1. Set <project>my-project-name</project>\n"
              + "2. Set <project>"
              + APPENGINE_CONFIG
              + "</project> to use <application> from appengine-web.xml\n"
              + "3. Set <project>"
              + GCLOUD_CONFIG
              + "</project> to use project from gcloud config.");
    } else if (deployMojo.project.equals(APPENGINE_CONFIG)) {
      try {
        AppEngineDescriptor appEngineDescriptor =
            AppEngineDescriptor.parse(new FileInputStream(appengineWebXml));
        String appengineWebXmlProject = appEngineDescriptor.getProjectId();
        if (appengineWebXmlProject == null || appengineWebXmlProject.trim().isEmpty()) {
          throw new MojoExecutionException("<application> was not found in appengine-web.xml");
        }
        deployMojo.project = appengineWebXmlProject;
      } catch (AppEngineException | IOException | SAXException e) {
        throw new MojoExecutionException("Failed to read project from appengine-web.xml");
      }
    } else if (deployMojo.project.equals(GCLOUD_CONFIG)) {
      deployMojo.project = null;
    }

    if (deployMojo.version == null || deployMojo.version.trim().isEmpty()) {
      throw new MojoExecutionException(
          "Deployment version must be defined or configured to read from system state\n"
              + "1. Set <version>my-version</version>\n"
              + "2. Set <version>"
              + APPENGINE_CONFIG
              + "</version> to use <version> from appengine-web.xml\n"
              + "3. Set <version>"
              + GCLOUD_CONFIG
              + "</version> to use version from gcloud config.");
    } else if (deployMojo.version.equals(APPENGINE_CONFIG)) {
      try {
        AppEngineDescriptor appEngineDescriptor =
            AppEngineDescriptor.parse(new FileInputStream(appengineWebXml));
        String appengineWebXmlVersion = appEngineDescriptor.getProjectVersion();
        if (appengineWebXmlVersion == null || appengineWebXmlVersion.trim().isEmpty()) {
          throw new MojoExecutionException("<version> was not found in appengine-web.xml");
        }
        deployMojo.version = appengineWebXmlVersion;
      } catch (AppEngineException | IOException | SAXException e) {
        throw new MojoExecutionException("Failed to read version from appengine-web.xml");
      }
    } else if (deployMojo.version.equals(GCLOUD_CONFIG)) {
      deployMojo.version = null;
    }
  }
}
