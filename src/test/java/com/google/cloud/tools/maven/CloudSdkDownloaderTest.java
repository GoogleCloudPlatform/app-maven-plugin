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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVerificationException;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVersionMismatchException;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponentInstaller;
import com.google.cloud.tools.managedcloudsdk.components.SdkUpdater;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstaller;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkDownloaderTest {

  @Mock private Log log;
  @Mock private ManagedCloudSdk managedCloudSdk;

  @Mock private SdkInstaller installer;
  @Mock private SdkComponentInstaller componentInstaller;
  @Mock private SdkUpdater updater;

  @InjectMocks private CloudSdkDownloader downloader;

  @Before
  public void setup() {
    when(managedCloudSdk.newInstaller()).thenReturn(installer);
    when(managedCloudSdk.newComponentInstaller()).thenReturn(componentInstaller);
    when(managedCloudSdk.newUpdater()).thenReturn(updater);
  }

  @Test
  public void testDownloadCloudSdk_install()
      throws ManagedSdkVerificationException, ManagedSdkVersionMismatchException {
    when(managedCloudSdk.isInstalled()).thenReturn(false);
    downloader.downloadCloudSdk(log);
    verify(managedCloudSdk).newInstaller();
  }

  @Test
  public void testDownloadCloudSdk_installComponent()
      throws ManagedSdkVerificationException, ManagedSdkVersionMismatchException {
    when(managedCloudSdk.isInstalled()).thenReturn(true);
    when(managedCloudSdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)).thenReturn(false);
    downloader.downloadCloudSdk(log);
    verify(managedCloudSdk, never()).newInstaller();
    verify(managedCloudSdk).newComponentInstaller();
  }

  @Test
  public void testDownloadCloudSdk_update()
      throws ManagedSdkVerificationException, ManagedSdkVersionMismatchException {
    when(managedCloudSdk.isInstalled()).thenReturn(true);
    when(managedCloudSdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)).thenReturn(true);
    when(managedCloudSdk.isUpToDate()).thenReturn(false);
    downloader.downloadCloudSdk(log);
    verify(managedCloudSdk, never()).newInstaller();
    verify(managedCloudSdk, never()).newComponentInstaller();
    verify(managedCloudSdk).newUpdater();
  }
}
