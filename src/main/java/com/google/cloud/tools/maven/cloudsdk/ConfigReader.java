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

package com.google.cloud.tools.maven.cloudsdk;

import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import java.io.IOException;

public class ConfigReader {
  public static final String APPENGINE_CONFIG = "APPENGINE_CONFIG";
  public static final String GCLOUD_CONFIG = "GCLOUD_CONFIG";

  private final Gcloud gcloud;

  // use CloudSdkAppEngineFactory to instantiate
  ConfigReader(Gcloud gcloud) {
    this.gcloud = gcloud;
  }

  /** Return gcloud config property for project, or error out if not found. */
  public String getProjectId() {
    try {
      String gcloudProject = gcloud.getConfig().getProject();
      if (gcloudProject == null || gcloudProject.trim().isEmpty()) {
        throw new RuntimeException("Project was not found in gcloud config");
      }
      return gcloudProject;
    } catch (CloudSdkNotFoundException
        | CloudSdkOutOfDateException
        | CloudSdkVersionFileException
        | IOException
        | ProcessHandlerException ex) {
      throw new RuntimeException("Failed to read project from gcloud config", ex);
    }
  }
}
