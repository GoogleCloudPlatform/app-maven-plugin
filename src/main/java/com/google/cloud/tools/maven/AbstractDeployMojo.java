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
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugins.annotations.Parameter;
import org.xml.sax.SAXException;

public abstract class AbstractDeployMojo extends StageMojo implements DeployConfiguration {
  /**
   * The yaml files for the services or configurations you want to deploy. If not given, defaults to
   * app.yaml in the staging directory. If that is not found, attempts to automatically generate
   * necessary configuration files (such as app.yaml) in the staging directory.
   */
  @Parameter(alias = "deploy.deployables", property = "app.deploy.deployables")
  protected List<File> deployables;

  /**
   * The Google Cloud Storage bucket used to stage files associated with the deployment. If this
   * argument is not specified, the application's default code bucket is used.
   */
  @Parameter(alias = "deploy.bucket", property = "app.deploy.bucket")
  protected String bucket;

  /**
   * Deploy with a specific Docker image. Docker url must be from one of the valid gcr hostnames.
   *
   * <p><i>Supported only for flexible environment.</i>
   */
  @Parameter(alias = "deploy.imageUrl", property = "app.deploy.imageUrl")
  protected String imageUrl;

  /** Promote the deployed version to receive all traffic. True by default. */
  @Parameter(alias = "deploy.promote", property = "app.deploy.promote")
  protected Boolean promote;

  /** The App Engine server to connect to. You will not typically need to change this value. */
  @Parameter(alias = "deploy.server", property = "app.deploy.server")
  protected String server;

  /** Stop the previously running version when deploying a new version that receives all traffic. */
  @Parameter(alias = "deploy.stopPreviousVersion", property = "app.deploy.stopPreviousVersion")
  protected Boolean stopPreviousVersion;

  /**
   * The version of the app that will be created or replaced by this deployment. If you do not
   * specify a version, one will be generated for you.
   */
  @Parameter(alias = "deploy.version", property = "app.deploy.version")
  protected String version;

  /**
   * The Google Cloud Platform project name to use for this invocation. If omitted then the current
   * project is assumed.
   */
  @Parameter(alias = "deploy.project", property = "app.deploy.project")
  protected String project;

  @Override
  public List<File> getDeployables() {
    return deployables;
  }

  @Override
  public String getBucket() {
    return bucket;
  }

  @Override
  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public Boolean getPromote() {
    return promote;
  }

  @Override
  public String getServer() {
    return server;
  }

  @Override
  public Boolean getStopPreviousVersion() {
    return stopPreviousVersion;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getProject() {
    return project;
  }

  void updatePropertiesFromAppEngineWebXml() throws IOException, SAXException, AppEngineException {
    AppEngineDescriptor appengineWebXmlDoc =
        AppEngineDescriptor.parse(
            new FileInputStream(
                sourceDirectory.toPath().resolve("WEB-INF").resolve("appengine-web.xml").toFile()));
    String xmlProject = appengineWebXmlDoc.getProjectId();
    String xmlVersion = appengineWebXmlDoc.getProjectVersion();

    // Verify that project is set somewhere
    if (project == null && xmlProject == null) {
      throw new RuntimeException(
          "appengine-plugin does not use gcloud global project state. Please configure the "
              + "application ID in your build.gradle or appengine-web.xml.");
    }

    boolean readAppEngineWebXml = Boolean.getBoolean("deploy.read.appengine.web.xml");
    if (readAppEngineWebXml && (project != null || version != null)) {
      // Should read from appengine-web.xml, but configured in pom.xml
      throw new RuntimeException(
          "Cannot override appengine.deploy config with appengine-web.xml. Either remove "
              + "the project/version properties from your build.gradle, or clear the "
              + "deploy.read.appengine.web.xml system property to read from build.gradle.");
    } else if (!readAppEngineWebXml
        && (project == null && xmlProject != null || version == null && xmlVersion != null)) {
      // System property not set, but configuration is only in appengine-web.xml
      throw new RuntimeException(
          "Project/version is set in application-web.xml, but deploy.read.appengine.web.xml is "
              + "false. If you would like to use the state from appengine-web.xml, please set the "
              + "system property deploy.read.appengine.web.xml=true.");
    }

    if (readAppEngineWebXml) {
      project = xmlProject;
      version = xmlVersion;
    }
  }
}
