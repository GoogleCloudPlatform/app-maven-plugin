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

import java.nio.file.Files;
import org.apache.maven.plugin.MojoExecutionException;

public interface Stager {

  /**
   * Create a new appengine-web.xml or app.yaml based stager depending on whether an
   * appengine-web.xml could be found in the project or not.
   */
  static Stager newStager(AbstractStageMojo stageConfiguration) throws MojoExecutionException {
    if (stageConfiguration.getArtifact() == null
        || !Files.exists(stageConfiguration.getArtifact())) {
      throw new MojoExecutionException(
          "\nCould not determine appengine environment, did you package your application?"
              + "\nRun 'mvn package appengine:stage'");
    }
    return stageConfiguration.isAppEngineWebXmlBased()
        ? AppEngineWebXmlStager.newStager(stageConfiguration)
        : AppYamlStager.newStager(stageConfiguration);
  }

  void stage() throws MojoExecutionException;
}
