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
import com.google.cloud.tools.appengine.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDevServer1;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDevServer2;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkGenRepoInfoFile;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.InvalidJavaSdkException;
import com.google.cloud.tools.appengine.cloudsdk.process.NonZeroExceptionExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import java.nio.file.Path;
import org.apache.maven.plugin.logging.Log;

/** Factory for App Engine dependencies. */
public class CloudSdkAppEngineFactory implements AppEngineFactory {

  private CloudSdkFactory cloudSdkFactory;
  private CloudSdkMojo mojo;
  private CloudSdkOperationsFactory cloudSdkOperationsFactory;

  /**
   * Constructs a new CloudSdkAppEngineFactory
   *
   * @param mojo The mojo containing Cloud Sdk configuration parameters
   */
  public CloudSdkAppEngineFactory(CloudSdkMojo mojo) {
    this(mojo, new CloudSdkFactory(), new CloudSdkOperationsFactory());
  }

  private CloudSdkAppEngineFactory(
      CloudSdkMojo mojo,
      CloudSdkFactory cloudSdkFactory,
      CloudSdkOperationsFactory cloudSdkOperationsFactory) {
    this.mojo = mojo;
    this.cloudSdkFactory = cloudSdkFactory;
    this.cloudSdkOperationsFactory = cloudSdkOperationsFactory;
  }

  @Override
  public AppEngineStandardStaging standardStaging() {
    try {
      return cloudSdkFactory.standardStaging(defaultCloudSdkBuilder().build());
    } catch (CloudSdkNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public AppEngineFlexibleStaging flexibleStaging() {
    return cloudSdkFactory.flexibleStaging();
  }

  @Override
  public AppEngineDeployment deployment() {
    try {
      return cloudSdkFactory.deployment(defaultCloudSdkBuilder().build());
    } catch (CloudSdkNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public AppEngineDevServer devServerRunSync(SupportedDevServerVersion version) {
    return createDevServerForVersion(version);
  }

  private AppEngineDevServer createDevServerForVersion(SupportedDevServerVersion version) {
    try {
      return createDevServerForVersion(version, defaultCloudSdkBuilder().build());
    } catch (CloudSdkNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  private AppEngineDevServer createDevServerForVersion(
      SupportedDevServerVersion version, CloudSdk cloudSdk) {
    switch (version) {
      case V1:
        return cloudSdkFactory.devServer1(cloudSdk);
      case V2ALPHA:
        return cloudSdkFactory.devServer(cloudSdk);
      default:
        throw new IllegalArgumentException("Unsupported dev server version: " + version);
    }
  }

  @Override
  public AppEngineDevServer devServerRunAsync(
      int startSuccessTimeout, SupportedDevServerVersion version) {
    CloudSdk.Builder builder =
        defaultCloudSdkBuilder().async(true).runDevAppServerWait(startSuccessTimeout);
    try {
      return createDevServerForVersion(version, builder.build());
    } catch (CloudSdkNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public AppEngineDevServer devServerStop(SupportedDevServerVersion version) {
    return createDevServerForVersion(version);
  }

  @Override
  public GenRepoInfoFile genRepoInfoFile() {
    try {
      return cloudSdkFactory.genRepoInfoFile(defaultCloudSdkBuilder().build());
    } catch (CloudSdkNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected CloudSdk.Builder defaultCloudSdkBuilder() {

    mojo.handleCloudSdkPathDeprecation();

    Path sdkPath = mojo.getCloudSdkHome();
    if (mojo.getCloudSdkHome() == null) {
      sdkPath =
          cloudSdkOperationsFactory
              .newDownloader(mojo.getCloudSdkVersion())
              .downloadCloudSdk(mojo.getLog());
    }

    CloudSdk.Builder cloudSdkBuilder = cloudSdkFactory.cloudSdkBuilder().sdkPath(sdkPath);

    if (mojo.getCloudSdkHome() != null && mojo.getCloudSdkVersion() != null) {
      try {
        cloudSdkOperationsFactory
            .newChecker(mojo.getCloudSdkVersion())
            .checkCloudSdk(cloudSdkBuilder.build());
      } catch (CloudSdkNotFoundException
          | CloudSdkVersionFileException
          | InvalidJavaSdkException
          | AppEngineJavaComponentsNotInstalledException
          | CloudSdkOutOfDateException ex) {
        throw new RuntimeException(ex);
      }
    }

    ProcessOutputLineListener lineListener = new DefaultProcessOutputLineListener(mojo.getLog());

    return cloudSdkBuilder
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

    DefaultProcessOutputLineListener(Log log) {
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
      return new CloudSdkAppEngineDevServer2(cloudSdk);
    }

    public AppEngineDevServer devServer1(CloudSdk cloudSdk) {
      return new CloudSdkAppEngineDevServer1(cloudSdk);
    }

    public GenRepoInfoFile genRepoInfoFile(CloudSdk cloudSdk) {
      return new CloudSdkGenRepoInfoFile(cloudSdk);
    }
  }
}
