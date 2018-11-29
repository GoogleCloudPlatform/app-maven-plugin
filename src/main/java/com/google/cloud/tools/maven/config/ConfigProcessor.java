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

import com.google.cloud.tools.maven.deploy.AbstractDeployMojo;
import java.nio.file.Path;

public interface ConfigProcessor {
  String APPENGINE_CONFIG = "APPENGINE_CONFIG";
  String GCLOUD_CONFIG = "GCLOUD_CONFIG";

  /**
   * Process a projectId string.
   *
   * @param projectId use configured projectId
   * @return final processed projectId
   */
  String processProjectId(String projectId);

  /**
   * Process a version string.
   *
   * @param version use configured version
   * @return final processed version
   */
  String processVersion(String version);

  /**
   * Process the app engine directory.
   *
   * @param deployMojo the deployMojo under considerations (contains the config)
   * @return the location of yaml config files (not including app.yaml)
   */
  Path processAppEngineDirectory(AbstractDeployMojo deployMojo);
}
