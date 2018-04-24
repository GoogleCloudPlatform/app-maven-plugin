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
  public void deploy() throws MojoFailureException, MojoExecutionException {
    stager.stage();
    if (deployMojo.deployables.isEmpty()) {
      deployMojo.deployables.add(deployMojo.stagingDirectory);
    }

    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new MojoFailureException(ex.getMessage(), ex);
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
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployCron() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDispatch() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDos() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployIndex() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployQueue() throws MojoFailureException, MojoExecutionException {
    stager.configureAppEngineDirectory();
    stager.stage();
    try {
      updatePropertiesFromAppEngineWebXml();
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /** Validates project/version configuration and pulls from appengine-web.xml if necessary */
  @VisibleForTesting
  void updatePropertiesFromAppEngineWebXml() throws IOException, SAXException, AppEngineException {
    AppEngineDescriptor appengineWebXmlDoc =
        AppEngineDescriptor.parse(
            new FileInputStream(
                deployMojo
                    .getSourceDirectory()
                    .toPath()
                    .resolve("WEB-INF")
                    .resolve("appengine-web.xml")
                    .toFile()));
    String xmlProject = appengineWebXmlDoc.getProjectId();
    String xmlVersion = appengineWebXmlDoc.getProjectVersion();

    // Verify that project is set somewhere
    if (deployMojo.project == null && xmlProject == null) {
      throw new RuntimeException(
          "appengine-plugin does not use gcloud global project state. Please configure the "
              + "application ID in your pom.xml or appengine-web.xml.");
    }

    boolean readAppEngineWebXml = Boolean.getBoolean("deploy.read.appengine.web.xml");
    if (readAppEngineWebXml && (deployMojo.project != null || deployMojo.version != null)) {
      // Should read from appengine-web.xml, but configured in pom.xml
      throw new RuntimeException(
          "Cannot override appengine.deploy config with appengine-web.xml. Either remove "
              + "the project/version properties from your pom.xml, or clear the "
              + "deploy.read.appengine.web.xml system property to read from pom.xml.");
    } else if (!readAppEngineWebXml
        && (deployMojo.project == null || deployMojo.version == null && xmlVersion != null)) {
      // System property not set, but configuration is only in appengine-web.xml
      throw new RuntimeException(
          "Project/version is set in application-web.xml, but deploy.read.appengine.web.xml is "
              + "false. If you would like to use the state from appengine-web.xml, please set the "
              + "system property deploy.read.appengine.web.xml=true.");
    }

    if (readAppEngineWebXml) {
      deployMojo.project = xmlProject;
      deployMojo.version = xmlVersion;
    }
  }
}
