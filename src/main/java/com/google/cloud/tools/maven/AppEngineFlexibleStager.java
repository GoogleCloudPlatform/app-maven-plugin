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
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class AppEngineFlexibleStager implements AppEngineStager {

  private StageMojo stageMojo;

  AppEngineFlexibleStager(StageMojo stageMojo) {
    this.stageMojo = stageMojo;
  }

  @Override
  public void stage() throws MojoExecutionException, MojoFailureException {
    stageMojo.getLog().info("Staging the application to: " + stageMojo.stagingDirectory);
    stageMojo.getLog().info("Detected App Engine flexible environment application.");

    if (!"war".equals(stageMojo.getPackaging()) && !"jar".equals(stageMojo.getPackaging())) {
      stageMojo.getLog().info("Stage/deploy is only executed for war and jar modules.");
      return;
    }

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
      configureAppEngineDirectory();
    }

    try {
      stageMojo.getAppEngineFactory().flexibleStaging().stageFlexible(stageMojo);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void configureAppEngineDirectory() {
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
}
