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

import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployProjectConfigurationConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractDeployMojo extends AbstractStageMojo
    implements DeployConfiguration, DeployProjectConfigurationConfiguration {

  /** This is not configurable by the user. */
  private List<File> deployables = new ArrayList<>();

  /**
   * The Google Cloud Storage bucket used to stage files associated with the deployment. If this
   * argument is not specified, the application's default code bucket is used.
   */
  @Parameter(alias = "deploy.bucket", property = "app.deploy.bucket")
  private String bucket;

  /**
   * Deploy with a specific Docker image. Docker url must be from one of the valid gcr hostnames.
   *
   * <p><i>Supported only for app.yaml based deployments.</i>
   */
  @Parameter(alias = "deploy.imageUrl", property = "app.deploy.imageUrl")
  private String imageUrl;

  /** Promote the deployed version to receive all traffic. True by default. */
  @Parameter(alias = "deploy.promote", property = "app.deploy.promote")
  private Boolean promote;

  /** The App Engine server to connect to. You will not typically need to change this value. */
  @Parameter(alias = "deploy.server", property = "app.deploy.server")
  private String server;

  /** Stop the previously running version when deploying a new version that receives all traffic. */
  @Parameter(alias = "deploy.stopPreviousVersion", property = "app.deploy.stopPreviousVersion")
  private Boolean stopPreviousVersion;

  /**
   * The version of the app that will be created or replaced by this deployment. If you do not
   * specify a version, one will be generated for you.
   */
  @Parameter(alias = "deploy.version", property = "app.deploy.version")
  private String version;

  /** The Google Cloud Platform project Id to use for this invocation */
  @Deprecated
  @Parameter(alias = "deploy.project", property = "app.deploy.project")
  private String project;

  /** The Google Cloud Platform project Id to use for this invocation. */
  @Parameter(alias = "deploy.projectId", property = "app.deploy.projectId")
  private String projectId;

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
  public String getProjectId() {
    if (project != null) {
      if (projectId != null) {
        throw new IllegalArgumentException(
            "Configuring <project> and <projectId> is not allowed, please use only <projectId>");
      }
      getLog()
          .warn(
              "Configuring <project> is deprecated,"
                  + " use <projectId> to set your Google Cloud ProjectId");
      return project;
    }
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setDeployables(List<File> deployables) {
    this.deployables = deployables;
  }
}
