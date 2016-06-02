/*
 * Copyright (C) 2016 Google Inc.
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

import com.google.cloud.tools.app.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.app.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.app.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessOutputLineListener;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

/**
 * Factory for plugin dependencies.
 */
public class PluginDependenciesFactory {

  /**
   * Default output listener that copies output to the Maven Mojo logger with a 'GCLOUD: ' prefix.
   */
  public ProcessOutputLineListener gcloudOutputListener(final AbstractMojo mojo) {
    return new ProcessOutputLineListener() {
      @Override
      public void outputLine(String line) {
        PluginDependenciesFactory.this.logger(mojo).info("GCLOUD: " + line);
      }
    };
  }

  public CloudSdk.Builder cloudSdkBuilder() {
    return new CloudSdk.Builder();
  }

  public AppEngineStandardStaging standardStaging(CloudSdk cloudSdk) {
    return new CloudSdkAppEngineStandardStaging(cloudSdk);
  }

  public AppEngineFlexibleStaging flexibleStaging() {
    return new CloudSdkAppEngineFlexibleStaging();
  }

  public AppEngineDeployment deployment(CloudSdk cloudSdk) {
    return new CloudSdkAppEngineDeployment(cloudSdk);
  }

  public AppEngineDevServer devServer(CloudSdk cloudSdk) {
    return new CloudSdkAppEngineDevServer(cloudSdk);
  }

  public AppEngineDevServer devServer() {
    return new CloudSdkAppEngineDevServer(null);
  }

  public Log logger(AbstractMojo mojo) {
    return mojo.getLog();
  }
}
