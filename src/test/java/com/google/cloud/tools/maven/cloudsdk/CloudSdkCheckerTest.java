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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.operations.CloudSdk;
import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.serialization.CloudSdkVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkCheckerTest {

  @Mock private CloudSdk sdk;

  private CloudSdkChecker cloudSdkChecker = new CloudSdkChecker();

  @Test
  public void testCheckCloudSdk_versionMismatch()
      throws CloudSdkVersionFileException, CloudSdkOutOfDateException, CloudSdkNotFoundException {
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("190.0.0"));
    try {
      cloudSdkChecker.checkCloudSdk(sdk, "191.0.0");
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          "Specified Cloud SDK version (191.0.0) does not match installed version (190.0.0).",
          ex.getMessage());
    }
  }

  @Test
  public void testCheckCloudSdk_callPluginsCoreChecks()
      throws CloudSdkVersionFileException, CloudSdkNotFoundException, CloudSdkOutOfDateException {
    when(sdk.getVersion()).thenReturn(new CloudSdkVersion("192.0.0"));

    cloudSdkChecker.checkCloudSdk(sdk, "192.0.0");

    verify(sdk).getVersion();
    verify(sdk).validateCloudSdk();
    verifyNoMoreInteractions(sdk);
  }

  @Test
  public void testCheckForAppEngine_smokeTest()
      throws AppEngineJavaComponentsNotInstalledException {
    cloudSdkChecker.checkForAppEngine(sdk);
    verify(sdk).validateAppEngineJavaComponents();
  }
}
