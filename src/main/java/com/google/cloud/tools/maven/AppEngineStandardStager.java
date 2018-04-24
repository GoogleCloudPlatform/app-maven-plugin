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
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AppEngineStandardStager implements AppEngineStager {

  private StageMojo stageMojo;

  public AppEngineStandardStager(StageMojo stageMojo) {
    this.stageMojo = stageMojo;
  }

  @Override
  public void stage() throws MojoExecutionException, MojoFailureException {
    stageMojo.getLog().info("Staging the application to: " + stageMojo.stagingDirectory);
    stageMojo.getLog().info("Detected App Engine standard environment application.");

    // delete staging directory if it exists
    if (stageMojo.stagingDirectory.exists()) {
      stageMojo.getLog().info("Deleting the staging directory: " + stageMojo.stagingDirectory);
      try {
        FileUtils.deleteDirectory(stageMojo.stagingDirectory);
      } catch (IOException ex) {
        throw new MojoFailureException("Unable to delete staging directory.", ex);
      }
    }
    if (!stageMojo.stagingDirectory.mkdir()) {
      throw new MojoExecutionException("Unable to create staging directory");
    }

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
  public void configureAppEngineDirectory() {
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
    } catch (XPathExpressionException ex) {
      throw new MojoExecutionException("XPath evaluation failed on appengine-web.xml", ex);
    } catch (SAXException | IOException | ParserConfigurationException ex) {
      throw new MojoExecutionException("Failed to parse appengine-web.xml", ex);
    }
  }
}
