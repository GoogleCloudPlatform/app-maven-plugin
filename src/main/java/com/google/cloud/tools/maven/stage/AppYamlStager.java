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

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.AppYamlProjectStageConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

public class AppYamlStager implements Stager {

  private final AbstractStageMojo stageMojo;
  private final ConfigBuilder configBuilder;

  public static AppYamlStager newAppYamlStager(AbstractStageMojo stageMojo) {
    return new AppYamlStager(stageMojo, new ConfigBuilder(stageMojo));
  }

  AppYamlStager(AbstractStageMojo stageMojo, ConfigBuilder configBuilder) {
    this.stageMojo = stageMojo;
    this.configBuilder = configBuilder;
  }

  @Override
  public void stage() throws MojoExecutionException {
    AppYamlProjectStageConfiguration config = configBuilder.buildConfiguration();
    Path stagingDirectory = config.getStagingDirectory();

    stageMojo.getLog().info("Staging the application to: " + stagingDirectory);
    stageMojo.getLog().info("Detected App Engine app.yaml based application.");

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
      stageMojo.getAppEngineFactory().appYamlStaging().stageArchive(config);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }

  static class ConfigBuilder {

    private final AbstractStageMojo stageMojo;

    ConfigBuilder(AbstractStageMojo stageMojo) {
      this.stageMojo = stageMojo;
    }

    AppYamlProjectStageConfiguration buildConfiguration() {
      return AppYamlProjectStageConfiguration.builder(
              processAppYamlBasedAppEngineDirectory(),
              stageMojo.getArtifact(),
              stageMojo.getStagingDirectory())
          .extraFilesDirectories(stageMojo.getExtraFilesDirectories())
          .dockerDirectory(stageMojo.getDockerDirectory())
          .build();
    }

    private Path processAppYamlBasedAppEngineDirectory() {
      if (stageMojo.getAppEngineDirectory() == null) {
        return stageMojo
            .getMavenProject()
            .getBasedir()
            .toPath()
            .resolve("src")
            .resolve("main")
            .resolve("appengine");
      }
      return stageMojo.getAppEngineDirectory();
    }
  }
}
