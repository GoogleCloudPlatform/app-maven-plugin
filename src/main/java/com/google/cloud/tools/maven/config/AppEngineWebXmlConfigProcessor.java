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

import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.maven.deploy.AbstractDeployMojo;
import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;

public class AppEngineWebXmlConfigProcessor implements ConfigProcessor {

  private final Path appengineWebXml;
  private final Gcloud gcloud;
  private final ConfigReader configReader;

  /** Config processor for appengine-web.xml based applications. */
  public AppEngineWebXmlConfigProcessor(
      Path appengineWebXml, Gcloud gcloud, ConfigReader configReader) {
    this.appengineWebXml = appengineWebXml;
    this.gcloud = gcloud;
    this.configReader = configReader;
  }

  @VisibleForTesting
  static final String VERSION_ERROR =
      "Deployment version must be configured on the appengine-maven-plugin using"
          + " <deploy.version> or by setting the system property 'app.deploy.version'\n"
          + "1. Set version = my-version\n"
          + "2. Set version = "
          + APPENGINE_CONFIG
          + " to use <version> from appengine-web.xml"
          + "3. Set version = "
          + GCLOUD_CONFIG
          + " to have gcloud generate a version for you";

  @Override
  public String processVersion(String version) {
    if (version == null || version.trim().isEmpty()) {
      throw new IllegalArgumentException(VERSION_ERROR);
    } else if (version.equals(APPENGINE_CONFIG)) {
      return configReader.getVersion(appengineWebXml);
    } else if (version.equals(GCLOUD_CONFIG)) {
      return null;
    } else {
      return version;
    }
  }

  @VisibleForTesting
  static final String PROJECT_ERROR =
      "Deployment projectId must be configured on the appengine-maven-plugin using"
          + " <deploy.projectId> or by setting the system property 'app.deploy.projectId'\n"
          + "1. Set projectId = my-project-id\n"
          + "2. Set projectId = "
          + APPENGINE_CONFIG
          + " to use <application> from appengine-web.xml\n"
          + "3. Set projectId = "
          + GCLOUD_CONFIG
          + " to use project from your gcloud configuration";

  @Override
  public String processProjectId(String projectId) {
    if (projectId == null || projectId.trim().isEmpty()) {
      throw new IllegalArgumentException(PROJECT_ERROR);
    } else if (projectId.equals(APPENGINE_CONFIG)) {
      return configReader.getProjectId(appengineWebXml);
    } else if (projectId.equals(GCLOUD_CONFIG)) {
      return configReader.getProjectId(gcloud);
    } else {
      return projectId;
    }
  }

  @Override
  public Path processAppEngineDirectory(AbstractDeployMojo deployMojo) {
    return deployMojo.getStagingDirectory().resolve("WEB-INF").resolve("appengine-generated");
  }
}
