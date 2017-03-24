/*
 * Copyright (C) 2017 Google Inc.
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

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.DeployProjectConfigurationConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class AbstractSingleYamlDeployMojo
    extends StageMojo implements DeployProjectConfigurationConfiguration {

  /**
   * The Google Cloud Platform project name to use for this invocation. If omitted then the current
   * project is assumed.
   */
  @Parameter(alias = "deploy.project", property = "app.deploy.project")
  protected String project;

  public AbstractSingleYamlDeployMojo() {
    super();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // execute stage
    super.execute();
  
    if (isStandardStaging()) {
      appEngineDirectory =
          stagingDirectory.toPath().resolve("WEB-INF/appengine-generated").toFile();
    }
  
    doDeploy(getAppEngineFactory().deployment(), this);
  }

  protected abstract void doDeploy(AppEngineDeployment appEngineDeployment,
      DeployProjectConfigurationConfiguration configuration);

  @Override
  public File getAppEngineDirectory() {
    return appEngineDirectory;
  }

  @Override
  public String getProject() {
    return project;
  }

}