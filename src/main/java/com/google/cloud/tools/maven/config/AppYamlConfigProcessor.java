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

package com.google.cloud.tools.maven.config;

import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.maven.deploy.AbstractDeployMojo;
import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;

public class AppYamlConfigProcessor implements ConfigProcessor {

  private final Gcloud gcloud;
  private final ConfigReader configReader;

  public AppYamlConfigProcessor(Gcloud gcloud, ConfigReader configReader) {
    this.gcloud = gcloud;
    this.configReader = configReader;
  }

  @VisibleForTesting
  static final String PROJECT_ERROR =
      "Deployment projectId must be configured on the appengine-maven-plugin using "
          + "<deploy.projectId> or by setting the system property 'app.deploy.projectId'\n"
          + "1. Set projectId = my-project-id\n"
          + "2. Set projectId = "
          + GCLOUD_CONFIG
          + " to use project from your gcloud configuration\n"
          + "3. Using projectId = "
          + APPENGINE_CONFIG
          + " is not allowed for app.yaml based projects";

  @Override
  public String processProjectId(String projectId) {
    if (projectId == null || projectId.trim().isEmpty() || projectId.equals(APPENGINE_CONFIG)) {
      throw new IllegalArgumentException(PROJECT_ERROR);
    } else if (projectId.equals(GCLOUD_CONFIG)) {
      return configReader.getProjectId(gcloud);
    }
    return projectId;
  }

  @VisibleForTesting
  static final String VERSION_ERROR =
      "Deployment version must be configured on the appengine-maven-plugin using"
          + " <deploy.version> or by setting the system property 'app.deploy.version'\n"
          + "1. Set version = my-version\n"
          + "2. Set version = "
          + GCLOUD_CONFIG
          + " to have gcloud generate a version for you\n"
          + "3. Using version = "
          + APPENGINE_CONFIG
          + " is not allowed for app.yaml based projects";

  @Override
  public String processVersion(String version) {
    if (version == null || version.trim().isEmpty() || version.equals(APPENGINE_CONFIG)) {
      throw new IllegalArgumentException(VERSION_ERROR);
    } else if (version.equals(GCLOUD_CONFIG)) {
      return null;
    }
    return version;
  }

  @Override
  public Path processAppEngineDirectory(AbstractDeployMojo deployMojo) {
    if (deployMojo.getAppEngineDirectory() == null) {
      return deployMojo
          .getMavenProject()
          .getBasedir()
          .toPath()
          .resolve("src")
          .resolve("main")
          .resolve("appengine");
    }
    return deployMojo.getAppEngineDirectory();
  }
}
