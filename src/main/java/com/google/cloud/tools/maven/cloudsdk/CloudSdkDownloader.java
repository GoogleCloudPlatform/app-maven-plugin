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

import com.google.cloud.tools.managedcloudsdk.BadCloudSdkVersionException;
import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVerificationException;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVersionMismatchException;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.UnsupportedOsException;
import com.google.cloud.tools.managedcloudsdk.Version;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import org.apache.maven.plugin.logging.Log;

public class CloudSdkDownloader {

  private final Function<String, ManagedCloudSdk> managedCloudSdkFactory;

  public CloudSdkDownloader(Function<String, ManagedCloudSdk> managedCloudSdkFactory) {
    this.managedCloudSdkFactory = managedCloudSdkFactory;
  }

  /**
   * Downloads/installs/updates the Cloud SDK
   *
   * @return The cloud SDK installation directory
   */
  public Path downloadIfNecessary(String version, Log log) {
    ManagedCloudSdk managedCloudSdk = managedCloudSdkFactory.apply(version);
    try {
      ProgressListener progressListener = new NoOpProgressListener();
      ConsoleListener consoleListener = new CloudSdkDownloaderConsoleListener(log);

      if (!managedCloudSdk.isInstalled()) {
        managedCloudSdk.newInstaller().install(progressListener, consoleListener);
      }

      if (!managedCloudSdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)) {
        managedCloudSdk
            .newComponentInstaller()
            .installComponent(SdkComponent.APP_ENGINE_JAVA, progressListener, consoleListener);
      }

      if (!managedCloudSdk.isUpToDate()) {
        managedCloudSdk.newUpdater().update(progressListener, consoleListener);
      }

      return managedCloudSdk.getSdkHome();
    } catch (IOException
        | SdkInstallerException
        | ManagedSdkVersionMismatchException
        | InterruptedException
        | CommandExecutionException
        | CommandExitException
        | ManagedSdkVerificationException ex) {
      throw new RuntimeException(ex);
    }
  }

  // for delayed instantiation because it can error unnecessarily
  static Function<String, ManagedCloudSdk> newManagedSdkFactory() {
    return (version) -> {
      try {
        if (Strings.isNullOrEmpty(version)) {
          return ManagedCloudSdk.newManagedSdk();
        } else {
          return ManagedCloudSdk.newManagedSdk(new Version(version));
        }
      } catch (UnsupportedOsException | BadCloudSdkVersionException ex) {
        throw new RuntimeException(ex);
      }
    };
  }
}
