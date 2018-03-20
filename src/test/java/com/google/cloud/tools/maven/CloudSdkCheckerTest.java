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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkCheckerTest {

  @Mock private CloudSdk sdk;

  private CloudSdkChecker cloudSdkChecker;

  @Test
  public void testCheckCloudSdk_versionMismatch() {
    cloudSdkChecker = new CloudSdkChecker("191.0.0");
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("190.0.0"));
    try {
      cloudSdkChecker.checkCloudSdk(sdk);
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          "Specified Cloud SDK version (191.0.0) does not match installed version (190.0.0).",
          ex.getMessage());
    }
  }

  @Test
  public void testCheckCloudSdk_sdkInstallationException() {
    cloudSdkChecker = new CloudSdkChecker("192.0.0");
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("192.0.0"));

    doThrow(CloudSdkNotFoundException.class).when(sdk).validateCloudSdk();
    try {
      cloudSdkChecker.checkCloudSdk(sdk);
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(ex.getCause().getClass(), CloudSdkNotFoundException.class);
    }
  }

  @Test
  public void testCheckCloudSdk_outOfDateException() {
    cloudSdkChecker = new CloudSdkChecker("192.0.0");
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("192.0.0"));

    doThrow(CloudSdkOutOfDateException.class).when(sdk).validateCloudSdk();
    try {
      cloudSdkChecker.checkCloudSdk(sdk);
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(ex.getCause().getClass(), CloudSdkOutOfDateException.class);
    }
  }

  @Test
  public void testCheckCloudSdk_versionFileException() {
    cloudSdkChecker = new CloudSdkChecker("192.0.0");
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("192.0.0"));

    doThrow(CloudSdkVersionFileException.class).when(sdk).validateCloudSdk();
    try {
      cloudSdkChecker.checkCloudSdk(sdk);
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(ex.getCause().getClass(), CloudSdkVersionFileException.class);
    }
  }

  @Test
  public void testCheckCloudSdk_appEngineInstallationExceptions() {
    cloudSdkChecker = new CloudSdkChecker("192.0.0");
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("192.0.0"));

    doThrow(AppEngineJavaComponentsNotInstalledException.class)
        .when(sdk)
        .validateAppEngineJavaComponents();
    try {
      cloudSdkChecker.checkCloudSdk(sdk);
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          ex.getCause().getClass(), AppEngineJavaComponentsNotInstalledException.class);
    }
  }
}
