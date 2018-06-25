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
import com.google.cloud.tools.appengine.cloudsdk.AppCfg;
import com.google.cloud.tools.appengine.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAuth;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.appengine.cloudsdk.LocalRun;
import com.google.cloud.tools.appengine.cloudsdk.process.LegacyProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.NonZeroExceptionExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.logging.Log;

/** Factory for App Engine dependencies. */
public class CloudSdkAppEngineFactory {

  private final CloudSdkMojo mojo;

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

  public CloudSdkAppEngineFactory(CloudSdkMojo mojo) {
    this.mojo = mojo;
  }

  /** Constructs an object used for auth */
  public CloudSdkAuth auth() {
    return getGcloud().newAuth(newDefaultProcessHandler());
  }

  /** Constructs an object used for standard staging */
  public AppEngineStandardStaging standardStaging() {
    return getAppCfg().newStaging(newDefaultProcessHandler());
  }

  /** Constructs an object used for flexible staging */
  public AppEngineFlexibleStaging flexibleStaging() {
    return new CloudSdkAppEngineFlexibleStaging();
  }

  /** Constructs an object used for deployment */
  public AppEngineDeployment deployment() {
    return getGcloud().newDeployment(newDefaultProcessHandler());
  }

  /** Constructs a dev server for the run goal */
  public AppEngineDevServer devServerRunSync(SupportedDevServerVersion version) {
    return createDevServerForVersion(version, newDefaultProcessHandler());
  }

  AppEngineDevServer createDevServerForVersion(
      SupportedDevServerVersion version, ProcessHandler processHandler) {
    switch (version) {
      case V1:
        return getLocalRun().newDevAppServer1(processHandler);
      case V2ALPHA:
        return getLocalRun().newDevAppServer2(processHandler);
      default:
        throw new IllegalArgumentException("Unsupported dev server version: " + version);
    }
  }

  /** Constructs a dev server in async mode */
  public AppEngineDevServer devServerRunAsync(
      int startSuccessTimeout, SupportedDevServerVersion version) {

    ProcessHandler ph = newDevAppServerAsyncHandler(startSuccessTimeout);
    return createDevServerForVersion(version, ph);
  }

  /** Constructs a dev server for the stop goal */
  public AppEngineDevServer devServerStop(SupportedDevServerVersion version) {
    return createDevServerForVersion(version, newDefaultProcessHandler());
  }

  /** Constructs an object used for the genRepoInfoFile goal */
  public GenRepoInfoFile genRepoInfoFile() {
    return getGcloud().newGenRepoInfo(newDefaultProcessHandler());
  }

  private CloudSdk buildCloudSdk() {
    return buildCloudSdk(
        mojo,
        new CloudSdkChecker(),
        new CloudSdkDownloader(CloudSdkDownloader.newManagedSdkFactory()));
  }

  static CloudSdk buildCloudSdk(
      CloudSdkMojo mojo, CloudSdkChecker cloudSdkChecker, CloudSdkDownloader cloudSdkDownloader) {

    try {
      if (mojo.getCloudSdkHome() != null) {
        // if user defined
        CloudSdk cloudSdk = new CloudSdk.Builder().sdkPath(mojo.getCloudSdkHome()).build();

        if (mojo.getCloudSdkVersion() != null) {
          cloudSdkChecker.checkCloudSdk(cloudSdk, mojo.getCloudSdkVersion());
        }
        return cloudSdk;
      } else {
        // we need to use a managed cloud sdk
        return new CloudSdk.Builder()
            .sdkPath(
                cloudSdkDownloader.downloadIfNecessary(mojo.getCloudSdkVersion(), mojo.getLog()))
            .build();
      }
    } catch (CloudSdkNotFoundException
        | CloudSdkVersionFileException
        | AppEngineJavaComponentsNotInstalledException
        | CloudSdkOutOfDateException ex) {
      throw new RuntimeException(ex);
    }
  }

  Gcloud getGcloud() {
    return Gcloud.builder(buildCloudSdk())
        .setMetricsEnvironment(mojo.getArtifactId(), mojo.getArtifactVersion())
        .setCredentialFile(mojo.getServiceAccountKeyFile())
        .build();
  }

  private AppCfg getAppCfg() {
    return AppCfg.builder(buildCloudSdk()).build();
  }

  private LocalRun getLocalRun() {
    return LocalRun.builder(buildCloudSdk()).build();
  }

  private ProcessHandler newDefaultProcessHandler() {
    ProcessOutputLineListener lineListener = new DefaultProcessOutputLineListener(mojo.getLog());
    return LegacyProcessHandler.builder()
        .addStdOutLineListener(lineListener)
        .addStdErrLineListener(lineListener)
        .setExitListener(new NonZeroExceptionExitListener())
        .build();
  }

  private ProcessHandler newDevAppServerAsyncHandler(int timeout) {
    Path logDir =
        Paths.get(mojo.getMavenProject().getBuild().getDirectory()).resolve("dev-appserver-out");
    if (!Files.exists(logDir)) {
      try {
        logDir = Files.createDirectories(logDir);
      } catch (IOException e) {
        throw new RuntimeException("Failed to create dev-appserver logging directory.");
      }
    }
    File logFile = logDir.resolve("dev_appserver.out").toFile();
    FileOutputLineListener fileListener = new FileOutputLineListener(logFile);
    mojo.getLog().info("Dev App Server output written to : " + logFile);

    ProcessOutputLineListener lineListener = new DefaultProcessOutputLineListener(mojo.getLog());

    return LegacyProcessHandler.builder()
        .addStdOutLineListener(lineListener)
        .addStdOutLineListener(fileListener)
        .addStdErrLineListener(lineListener)
        .addStdErrLineListener(fileListener)
        .setExitListener(new NonZeroExceptionExitListener())
        .buildDevAppServerAsync(timeout);
  }

  /**
   * Default output listener that copies output to the Maven Mojo logger with a 'GCLOUD: ' prefix.
   */
  static class DefaultProcessOutputLineListener implements ProcessOutputLineListener {

    private final Log log;

    DefaultProcessOutputLineListener(Log log) {
      this.log = log;
    }

    @Override
    public void onOutputLine(String line) {
      log.info("GCLOUD: " + line);
    }
  }

  /** A listener that redirects process output to a file. */
  static class FileOutputLineListener implements ProcessOutputLineListener {

    private final PrintStream logFilePrinter;

    FileOutputLineListener(final File logFile) {
      try {
        logFilePrinter = new PrintStream(logFile, StandardCharsets.UTF_8.name());
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread() {
                  public void run() {
                    logFilePrinter.close();
                  }
                });
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public void onOutputLine(String line) {
      logFilePrinter.println(line);
    }
  }
}
