/**
 * Copyright 2016 Google Inc.
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

import com.google.cloud.tools.app.Action;
import com.google.cloud.tools.app.GCloudExecutionException;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

/**
 * Abstract Mojo from which all goals inherit.
 */
public abstract class GcpAppMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  protected MavenProject mavenProject;
  /**
   * App ID to deploy to.
   */
  @Parameter(property = "gcp.app.project")
  protected String cloudProject;
  @Parameter(property = "gcp.app.modules")
  protected List<String> modules;
  /**
   * Version of the app that will be created or replaced by this deployment.
   */
  @Parameter(property = "gcp.app.version")
  protected String version;
  protected Action action;
  @Parameter(property = "gcp.app.server")
  protected String server;
  @Parameter(property = "gcp.app.cloudSdkOverride")
  protected String cloudSdkOverride;

  /**
   * Centralizes library invocation logic, including exception handling.
   *
   * <p>Assumes that {@code action} was initialized by the {@link Action} implementation.
   */
  protected void executeAction() throws MojoExecutionException {
    boolean didExecute = false;
    try {
      // Set Cloud SDK location override if there is any.
      if (!Strings.isNullOrEmpty(cloudSdkOverride)) {
        action.setCloudSdkOverride(cloudSdkOverride);
      }

      didExecute = action.execute();
    } catch (GCloudExecutionException|IOException gcEx) {
      getLog().debug(Throwables.getStackTraceAsString(gcEx));
      throw new MojoExecutionException(gcEx.getMessage(), gcEx);
    }

    if (!didExecute) {
      throw new MojoExecutionException("The operation did not execute correctly. Check the logs "
          + "for more information.");
    }
  }
}
