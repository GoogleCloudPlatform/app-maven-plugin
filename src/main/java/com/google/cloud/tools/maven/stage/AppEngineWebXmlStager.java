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

package com.google.cloud.tools.maven.stage;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AppEngineWebXmlStager implements Stager {

  private final AbstractStageMojo stageMojo;
  private final ConfigBuilder configBuilder;

  public static AppEngineWebXmlStager newAppEngineWebXmlStager(AbstractStageMojo stageMojo) {
    return new AppEngineWebXmlStager(stageMojo, new ConfigBuilder(stageMojo));
  }

  AppEngineWebXmlStager(AbstractStageMojo stageMojo, ConfigBuilder configBuilder) {
    this.stageMojo = stageMojo;
    this.configBuilder = configBuilder;
  }

  @Override
  public void stage() throws MojoExecutionException {
    StageStandardConfiguration config = configBuilder.buildConfiguration();
    Path stagingDirectory = config.getStagingDirectory();

    stageMojo.getLog().info("Staging the application to: " + stagingDirectory);
    stageMojo.getLog().info("Detected App Engine appengine-web.xml based application.");

    // delete staging directory if it exists
    if (Files.exists(stagingDirectory)) {
      stageMojo.getLog().info("Deleting the staging directory: " + stagingDirectory);
      try {
        FileUtils.deleteDirectory(stagingDirectory.toFile());
      } catch (IOException ex) {
        throw new MojoExecutionException("Unable to delete staging directory.", ex);
      }
    }
    if (!stagingDirectory.toFile().mkdir()) {
      throw new MojoExecutionException("Unable to create staging directory");
    }

    try {
      stageMojo.getAppEngineFactory().appengineWebXmlStaging().stageStandard(config);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  static class ConfigBuilder {

    private final AbstractStageMojo stageMojo;

    ConfigBuilder(AbstractStageMojo stageMojo) {
      this.stageMojo = stageMojo;
    }

    StageStandardConfiguration buildConfiguration() throws MojoExecutionException {
      return StageStandardConfiguration.builder(
              stageMojo.getSourceDirectory(), stageMojo.getStagingDirectory())
          .compileEncoding(stageMojo.getCompileEncoding())
          .deleteJsps(stageMojo.isDeleteJsps())
          .disableJarJsps(stageMojo.isDisableJarJsps())
          .disableUpdateCheck(stageMojo.isDisableUpdateCheck())
          .dockerfile(processDockerfile())
          .enableJarClasses(stageMojo.isEnableJarClasses())
          .enableJarSplitting(stageMojo.isEnableJarSplitting())
          .enableQuickstart(stageMojo.isEnableQuickstart())
          .jarSplittingExcludes(stageMojo.getJarSplittingExcludes())
          .runtime(processRuntime())
          .build();
    }

    Path processDockerfile() {
      // Dockerfile default location
      if (stageMojo.getDockerfile() == null) {
        if (stageMojo.getDockerfilePrimaryDefaultLocation() != null
            && Files.exists(stageMojo.getDockerfilePrimaryDefaultLocation())) {
          return stageMojo.getDockerfilePrimaryDefaultLocation();
        } else if (stageMojo.getDockerfileSecondaryDefaultLocation() != null
            && Files.exists(stageMojo.getDockerfileSecondaryDefaultLocation())) {
          return stageMojo.getDockerfileSecondaryDefaultLocation();
        }
      }
      return stageMojo.getDockerfile();
    }

    String processRuntime() throws MojoExecutionException {
      // force runtime to 'java' for compat projects using Java version >1.7
      Path appengineWebXml = stageMojo.getAppEngineWebXml();
      if (Float.parseFloat(stageMojo.getCompileTargetVersion()) > 1.7f && isVm(appengineWebXml)) {
        return "java";
      }
      return null;
    }

    private boolean isVm(Path appengineWebXml) throws MojoExecutionException {
      try {
        Document document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(appengineWebXml.toFile());
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
}
