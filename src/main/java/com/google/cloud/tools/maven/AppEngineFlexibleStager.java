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
import com.google.common.base.Preconditions;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

public class AppEngineFlexibleStager implements AppEngineStager {

  private boolean configured = false;
  private final AbstractStageMojo stageMojo;

  public AppEngineFlexibleStager(AbstractStageMojo stageConfiguration) {
    this.stageMojo = stageConfiguration;
  }

  @Override
  public void stage() throws MojoExecutionException {
    // since staging is all crazy, ensure the application developer has called the override first.
    Preconditions.checkState(configured, "Must call overrideAppEngineDirectory first");

    stageMojo.getLog().info("Staging the application to: " + stageMojo.getStagingDirectory());
    stageMojo.getLog().info("Detected App Engine app.yaml based application.");

    if (!"war".equals(stageMojo.getPackaging()) && !"jar".equals(stageMojo.getPackaging())) {
      stageMojo.getLog().info("Stage/deploy is only executed for war and jar modules.");
      return;
    }

    // delete staging directory if it exists
    if (stageMojo.getStagingDirectory().exists()) {
      stageMojo.getLog().info("Deleting the staging directory: " + stageMojo.getStagingDirectory());
      try {
        FileUtils.deleteDirectory(stageMojo.getStagingDirectory());
      } catch (IOException ex) {
        throw new MojoExecutionException("Unable to delete staging directory.", ex);
      }
    }
    if (!stageMojo.getStagingDirectory().mkdir()) {
      throw new MojoExecutionException("Unable to create staging directory");
    }

    try {
      stageMojo.getAppEngineFactory().flexibleStaging().stageFlexible(stageMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void overrideAppEngineDirectory() {
    configured = true;
    // can be user configured
    if (stageMojo.getAppEngineDirectory() == null) {
      stageMojo.setAppEngineDirectory(
          stageMojo
              .getMavenProject()
              .getBasedir()
              .toPath()
              .resolve("src")
              .resolve("main")
              .resolve("appengine")
              .toFile());
    }
  }
}
