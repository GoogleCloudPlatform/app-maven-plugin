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

import com.google.cloud.tools.appengine.api.debug.GenRepoInfoFile;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDevServer;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkGenRepoInfoFile;
import com.google.cloud.tools.appengine.cloudsdk.process.NonZeroExceptionExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;

import org.apache.maven.plugin.logging.Log;

/**
 * Factory for App Engine dependencies.
 */
public class CloudSdkAppEngineFactory implements AppEngineFactory {

  protected CloudSdkFactory cloudSdkFactory;
  private CloudSdkMojo mojo;

  public CloudSdkAppEngineFactory(CloudSdkMojo mojo) {
    this(mojo, new CloudSdkFactory());
  }

  public CloudSdkAppEngineFactory(CloudSdkMojo mojo, CloudSdkFactory cloudSdkFactory) {
    this.mojo = mojo;
    this.cloudSdkFactory = cloudSdkFactory;
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
    return cloudSdkFactory.devServer(defaultCloudSdkBuilder().build());
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

  @Override
  public GenRepoInfoFile genRepoInfoFile() {
    return cloudSdkFactory.genRepoInfoFile(defaultCloudSdkBuilder().build());
  }

  protected CloudSdk.Builder defaultCloudSdkBuilder() {

    ProcessOutputLineListener lineListener = new DefaultProcessOutputLineListener(mojo.getLog());

    return cloudSdkFactory.cloudSdkBuilder()
        .sdkPath(mojo.getCloudSdkPath())
        .addStdOutLineListener(lineListener)
        .addStdErrLineListener(lineListener)
        .exitListener(new NonZeroExceptionExitListener())
        .appCommandMetricsEnvironment(mojo.getArtifactId())
        .appCommandMetricsEnvironmentVersion(mojo.getArtifactVersion());
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
    public void onOutputLine(String line) {
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

    public GenRepoInfoFile genRepoInfoFile(CloudSdk cloudSdk) {
      return new CloudSdkGenRepoInfoFile(cloudSdk);
    }
  }

}
