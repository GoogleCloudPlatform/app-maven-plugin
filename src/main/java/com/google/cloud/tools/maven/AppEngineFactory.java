/*
 * Copyright 2016 Google LLC. All Rights Reserved.
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

import com.google.cloud.tools.appengine.api.debug.GenRepoInfoFile;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;

/** Factory interface for App Engine dependencies. */
public interface AppEngineFactory {

  /** Supported dev app server versions. */
  public enum SupportedDevServerVersion {
    V1,
    V2ALPHA;

    /**
     * Parses {@code versionString} into a {@link SupportedDevServerVersion}. The aim is to let the
     * users use lowercase in version strings.
     */
    public static SupportedDevServerVersion parse(String versionString) {
      if ("1".equals(versionString)) {
        return V1;
      } else if ("2-alpha".equals(versionString)) {
        return V2ALPHA;
      } else {
        throw new IllegalArgumentException("Unsupported version value: " + versionString);
      }
    }
  }

  AppEngineStandardStaging standardStaging();

  AppEngineFlexibleStaging flexibleStaging();

  AppEngineDeployment deployment();

  AppEngineDevServer devServerRunSync(SupportedDevServerVersion version);

  AppEngineDevServer devServerRunAsync(int startSuccessTimeout, SupportedDevServerVersion version);

  AppEngineDevServer devServerStop(SupportedDevServerVersion version);

  GenRepoInfoFile genRepoInfoFile();
}
