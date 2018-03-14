/*
 * Copyright 2017 Google LLC. All Rights Reserved.
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
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractSingleYamlDeployMojo extends StageMojo
    implements DeployProjectConfigurationConfiguration {

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
    doDeploy(getAppEngineFactory().deployment(), this);
  }

  /**
   * Sets {@code appEngineDirectory} based on whether the project is GAE Standard or Flexible if the
   * user has not set a value explicitly.
   *
   * <p>For Standard it uses {@code <stagingDirectory>/WEB-INF/appengine-generated}, for Flexible it
   * uses <code>${basedir}/src/main/appengine</code>.
   */
  @Override
  protected void configureAppEngineDirectory() {
    if (isStandardStaging()) {
      appEngineDirectory =
          stagingDirectory.toPath().resolve("WEB-INF/appengine-generated").toFile();
    } else {
      appEngineDirectory =
          mavenProject.getBasedir().toPath().resolve("src/main/appengine").toFile();
    }
  }

  protected abstract void doDeploy(
      AppEngineDeployment appEngineDeployment,
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
