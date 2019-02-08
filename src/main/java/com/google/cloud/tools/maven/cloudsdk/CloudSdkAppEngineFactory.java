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

package com.google.cloud.tools.maven.cloudsdk;

import com.google.cloud.tools.appengine.operations.AppCfg;
import com.google.cloud.tools.appengine.operations.AppEngineWebXmlProjectStaging;
import com.google.cloud.tools.appengine.operations.AppYamlProjectStaging;
import com.google.cloud.tools.appengine.operations.Auth;
import com.google.cloud.tools.appengine.operations.CloudSdk;
import com.google.cloud.tools.appengine.operations.Deployment;
import com.google.cloud.tools.appengine.operations.DevServer;
import com.google.cloud.tools.appengine.operations.DevServers;
import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.appengine.operations.GenRepoInfoFile;
import com.google.cloud.tools.appengine.operations.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.LegacyProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.NonZeroExceptionExitListener;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessOutputLineListener;
import com.google.common.annotations.VisibleForTesting;
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
  public Auth auth() {
    return getGcloud().newAuth(newDefaultProcessHandler());
  }

  /** Constructs an object used for appengine-web.xml based staging */
  public AppEngineWebXmlProjectStaging appengineWebXmlStaging() {
    return getAppCfg().newStaging(newDefaultProcessHandler());
  }

  /** Constructs an object used for app.yaml based staging */
  public AppYamlProjectStaging appYamlStaging() {
    return new AppYamlProjectStaging();
  }

  /** Constructs an object used for deployment */
  public Deployment deployment() {
    return getGcloud().newDeployment(newDefaultProcessHandler());
  }

  /** Constructs a dev server for the run goal */
  public DevServer devServerRunSync(SupportedDevServerVersion version) {
    return createDevServerForVersion(version, newDefaultProcessHandler());
  }

  DevServer createDevServerForVersion(
      SupportedDevServerVersion version, ProcessHandler processHandler) {
    switch (version) {
      case V1:
        return getDevServers().newDevAppServer1(processHandler);
      case V2ALPHA:
        return getDevServers().newDevAppServer2(processHandler);
      default:
        throw new IllegalArgumentException("Unsupported dev server version: " + version);
    }
  }

  /** Constructs a dev server in async mode */
  public DevServer devServerRunAsync(int startSuccessTimeout, SupportedDevServerVersion version) {

    ProcessHandler ph = newDevAppServerAsyncHandler(startSuccessTimeout);
    return createDevServerForVersion(version, ph);
  }

  /** Constructs a dev server for the stop goal */
  public DevServer devServerStop(SupportedDevServerVersion version) {
    return createDevServerForVersion(version, newDefaultProcessHandler());
  }

  /** Constructs an object used for the genRepoInfoFile goal */
  public GenRepoInfoFile genRepoInfoFile() {
    return getGcloud().newGenRepoInfo(newDefaultProcessHandler());
  }

  private CloudSdk buildCloudSdkMinimal() {
    return buildCloudSdk(
        mojo,
        new CloudSdkChecker(),
        new CloudSdkDownloader(CloudSdkDownloader.newManagedSdkFactory()),
        false);
  }

  @VisibleForTesting
  CloudSdk buildCloudSdkWithAppEngineComponents() {
    return buildCloudSdk(
        mojo,
        new CloudSdkChecker(),
        new CloudSdkDownloader(CloudSdkDownloader.newManagedSdkFactory()),
        true);
  }

  static CloudSdk buildCloudSdk(
      CloudSdkMojo mojo,
      CloudSdkChecker cloudSdkChecker,
      CloudSdkDownloader cloudSdkDownloader,
      boolean requiresAppEngineComponents) {

    try {
      if (mojo.getCloudSdkHome() != null) {
        // if user defined
        CloudSdk cloudSdk = new CloudSdk.Builder().sdkPath(mojo.getCloudSdkHome()).build();

        if (mojo.getCloudSdkVersion() != null) {
          cloudSdkChecker.checkCloudSdk(cloudSdk, mojo.getCloudSdkVersion());
        }
        if (requiresAppEngineComponents) {
          cloudSdkChecker.checkForAppEngine(cloudSdk);
        }
        return cloudSdk;
      } else {
        // we need to use a managed cloud sdk
        return new CloudSdk.Builder()
            .sdkPath(
                cloudSdkDownloader.downloadIfNecessary(
                    mojo.getCloudSdkVersion(), mojo.getLog(), requiresAppEngineComponents))
            .build();
      }
    } catch (CloudSdkNotFoundException
        | CloudSdkVersionFileException
        | AppEngineJavaComponentsNotInstalledException
        | CloudSdkOutOfDateException ex) {
      throw new RuntimeException(ex);
    }
  }

  /** Return a Gcloud instance using global configuration. */
  public Gcloud getGcloud() {
    return Gcloud.builder(buildCloudSdkMinimal())
        .setMetricsEnvironment(mojo.getArtifactId(), mojo.getArtifactVersion())
        .setCredentialFile(mojo.getServiceAccountKeyFile())
        .build();
  }

  private AppCfg getAppCfg() {
    return AppCfg.builder(buildCloudSdkWithAppEngineComponents()).build();
  }

  private DevServers getDevServers() {
    return DevServers.builder(buildCloudSdkWithAppEngineComponents()).build();
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

  public ConfigReader newConfigReader() {
    return new ConfigReader(getGcloud());
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
