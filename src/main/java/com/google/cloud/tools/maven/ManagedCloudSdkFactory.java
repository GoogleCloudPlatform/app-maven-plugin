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

import com.google.cloud.tools.managedcloudsdk.BadCloudSdkVersionException;
import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import com.google.cloud.tools.managedcloudsdk.UnsupportedOsException;
import com.google.cloud.tools.managedcloudsdk.Version;
import com.google.common.base.Strings;

public class ManagedCloudSdkFactory {
  private String version;

  public ManagedCloudSdkFactory(String version) {
    this.version = version;
  }

  /** Build a new ManagedCloudSdk from a given version */
  public ManagedCloudSdk newManagedSdk()
      throws UnsupportedOsException, BadCloudSdkVersionException {
    if (Strings.isNullOrEmpty(version)) {
      return ManagedCloudSdk.newManagedSdk();
    } else {
      return ManagedCloudSdk.newManagedSdk(new Version(version));
    }
  }
}
