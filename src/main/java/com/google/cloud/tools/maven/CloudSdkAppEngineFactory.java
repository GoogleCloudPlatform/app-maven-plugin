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

import org.apache.maven.plugin.logging.Log;

/**
 * Factory for App Engine dependencies.
 */
public class CloudSdkAppEngineFactory implements AppEngineFactory {

  protected CloudSdkFactory cloudSdkFactory = new CloudSdkFactory();
  protected CloudSdkMojo mojo;

  public CloudSdkAppEngineFactory(CloudSdkMojo mojo) {
    this.mojo = mojo;
  }

  @Override
  public AppEngineStandardStaging standardStaging() {
    return cloudSdkFactory.standardStaging(defaultCloudSdkBuilder().build());
  }

  @Override
  public AppEngineFlexibleStaging flexibleStaging() {
    return cloudSdkFactory.flexibleStaging();
  }

  @Override
  public AppEngineDeployment deployment() {
    return cloudSdkFactory.deployment(defaultCloudSdkBuilder().build());
  }

  @Override
  public AppEngineDevServer devServerRunSync() {
    return new CloudSdkAppEngineDevServer(defaultCloudSdkBuilder().build());
  }

  @Override
  public AppEngineDevServer devServerRunAsync(int startSuccessTimeout) {
    CloudSdk.Builder builder = defaultCloudSdkBuilder()
        .async(true)
        .runDevAppServerWait(startSuccessTimeout);
    return cloudSdkFactory.devServer(builder.build());
  }

  @Override
  public AppEngineDevServer devServerStop() {
    return cloudSdkFactory.devServer(null);
  }

  protected CloudSdk.Builder defaultCloudSdkBuilder() {

    ProcessOutputLineListener lineListener = new DefaultProcessOutputLineListener(mojo.getLog());

    return cloudSdkFactory.cloudSdkBuilder()
        .sdkPath(mojo.cloudSdkPath)
        .addStdOutLineListener(lineListener)
        .addStdErrLineListener(lineListener)
        .appCommandMetricsEnvironment(mojo.pluginDescriptor.getArtifactId())
        .appCommandMetricsEnvironmentVersion(mojo.pluginDescriptor.getVersion());
  }

  /**
   * Default output listener that copies output to the Maven Mojo logger with a 'GCLOUD: ' prefix.
   */
  protected static class DefaultProcessOutputLineListener implements ProcessOutputLineListener {

    private Log log;

    public DefaultProcessOutputLineListener(Log log) {
      this.log = log;
    }

    @Override
    public void outputLine(String line) {
      log.info("GCLOUD: " + line);
    }
  }

  protected static class CloudSdkFactory {

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

  }

}
