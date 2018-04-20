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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AppEngineStandardDeployer implements AppEngineDeployer {

  @Override
  public void stage(StageMojo stageMojo) throws MojoExecutionException, MojoFailureException {
    stageMojo.clearStagingDirectory();

    stageMojo.getLog().info("Staging the application to: " + stageMojo.stagingDirectory);
    stageMojo.getLog().info("Detected App Engine standard environment application.");

    if (stageMojo.appEngineDirectory == null) {
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

    // force runtime to 'java' for compat projects using Java version >1.7
    File appengineWebXml =
        new File(
            stageMojo
                .sourceDirectory
                .toPath()
                .resolve("WEB-INF")
                .resolve("appengine-web.xml")
                .toString());
    if (Float.parseFloat(stageMojo.getCompileTargetVersion()) > 1.7f && isVm(appengineWebXml)) {
      stageMojo.runtime = "java";
    }

    // Dockerfile default location
    if (stageMojo.dockerfile == null) {
      if (stageMojo.dockerfilePrimaryDefaultLocation != null
          && stageMojo.dockerfilePrimaryDefaultLocation.exists()) {
        stageMojo.dockerfile = stageMojo.dockerfilePrimaryDefaultLocation;
      } else if (stageMojo.dockerfileSecondaryDefaultLocation != null
          && stageMojo.dockerfileSecondaryDefaultLocation.exists()) {
        stageMojo.dockerfile = stageMojo.dockerfileSecondaryDefaultLocation;
      }
    }

    try {
      stageMojo.getAppEngineFactory().standardStaging().stageStandard(stageMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deploy(AbstractDeployMojo deployMojo) throws MojoFailureException {
    if (deployMojo.deployables.isEmpty()) {
      deployMojo.deployables.add(deployMojo.stagingDirectory);
    }

    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new MojoFailureException(ex.getMessage(), ex);
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
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deploy(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployCron(AbstractDeployMojo deployMojo) {
    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deployCron(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDispatch(AbstractDeployMojo deployMojo) {
    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deployDispatch(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployDos(AbstractDeployMojo deployMojo) {
    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deployDos(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployIndex(AbstractDeployMojo deployMojo) {
    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deployIndex(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void deployQueue(AbstractDeployMojo deployMojo) {
    try {
      updatePropertiesFromAppEngineWebXml(deployMojo);
      deployMojo.getAppEngineFactory().deployment().deployQueue(deployMojo);
    } catch (AppEngineException | SAXException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void configureAppEngineDirectory(StageMojo stageMojo) {
    stageMojo.appEngineDirectory =
        stageMojo
            .stagingDirectory
            .toPath()
            .resolve("WEB-INF")
            .resolve("appengine-generated")
            .toFile();
  }

  private boolean isVm(File appengineWebXml) throws MojoExecutionException {
    try {
      Document document =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(appengineWebXml);
      XPath xpath = XPathFactory.newInstance().newXPath();
      String expression = "/appengine-web-app/vm/text()='true'";
      return (Boolean) xpath.evaluate(expression, document, XPathConstants.BOOLEAN);
    } catch (XPathExpressionException e) {
      throw new MojoExecutionException("XPath evaluation failed on appengine-web.xml", e);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new MojoExecutionException("Failed to parse appengine-web.xml", e);
    }
  }

  /** */
  @VisibleForTesting
  void updatePropertiesFromAppEngineWebXml(AbstractDeployMojo deployMojo)
      throws IOException, SAXException, AppEngineException {
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

    String project = deployMojo.project;
    String version = deployMojo.version;

    // Verify that project is set somewhere
    if (project == null && xmlProject == null) {
      throw new RuntimeException(
          "appengine-plugin does not use gcloud global project state. Please configure the "
              + "application ID in your pom.xml or appengine-web.xml.");
    }

    boolean readAppEngineWebXml = Boolean.getBoolean("deploy.read.appengine.web.xml");
    if (readAppEngineWebXml && (project != null || version != null)) {
      // Should read from appengine-web.xml, but configured in pom.xml
      throw new RuntimeException(
          "Cannot override appengine.deploy config with appengine-web.xml. Either remove "
              + "the project/version properties from your pom.xml, or clear the "
              + "deploy.read.appengine.web.xml system property to read from build.gradle.");
    } else if (!readAppEngineWebXml && (project == null || version == null && xmlVersion != null)) {
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
