/*
 * Copyright 2016 Google LLC. All Rights Reserved.
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
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Stage and deploy an application to Google App Engine standard or flexible environment. */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployMojo extends StageMojo implements DeployConfiguration {

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
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!"war".equals(getPackaging()) && !"jar".equals(getPackaging())) {
      // https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/85
      getLog().info("Deploy is only executed for war and jar modules.");
      return;
    }
    // execute stage
    super.execute();

    if (deployables.isEmpty()) {
      deployables.add(stagingDirectory);
    }

    try {
      getAppEngineFactory().deployment().deploy(this);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

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
}
