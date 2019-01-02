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

import com.google.cloud.tools.appengine.operations.CloudSdk;
import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;

public class CloudSdkChecker {

  /**
   * Validates the cloud SDK installation
   *
   * @param cloudSdk CloudSdk with a configured sdk home directory
   */
  public void checkCloudSdk(CloudSdk cloudSdk, String version)
      throws CloudSdkVersionFileException, CloudSdkNotFoundException, CloudSdkOutOfDateException {
    if (!version.equals(cloudSdk.getVersion().toString())) {
      throw new RuntimeException(
          "Specified Cloud SDK version ("
              + version
              + ") does not match installed version ("
              + cloudSdk.getVersion()
              + ").");
    }

    cloudSdk.validateCloudSdk();
  }

  public void checkForAppEngine(CloudSdk cloudSdk)
      throws AppEngineJavaComponentsNotInstalledException {
    cloudSdk.validateAppEngineJavaComponents();
  }
}
