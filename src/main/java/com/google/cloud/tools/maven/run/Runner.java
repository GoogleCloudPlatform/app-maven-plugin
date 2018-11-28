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

package com.google.cloud.tools.maven.run;

import static com.google.cloud.tools.maven.config.ConfigProcessor.APPENGINE_CONFIG;
import static com.google.cloud.tools.maven.config.ConfigProcessor.GCLOUD_CONFIG;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.RunConfiguration;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory.SupportedDevServerVersion;
import com.google.cloud.tools.maven.config.ConfigReader;
import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;

public class Runner {

  static class Factory {
    public Runner newRunner(AbstractRunMojo runMojo) {
      return new Runner(runMojo, new ConfigBuilder(runMojo));
    }
  }

  private final AbstractRunMojo runMojo;
  private final ConfigBuilder configBuilder;

  Runner(AbstractRunMojo runMojo, ConfigBuilder configBuilder) {
    this.runMojo = runMojo;
    this.configBuilder = configBuilder;
  }

  /** Run the dev appserver. */
  public void run() throws MojoExecutionException {
    SupportedDevServerVersion convertedVersion = convertVersionString();

    try {
      runMojo
          .getAppEngineFactory()
          .devServerRunSync(convertedVersion)
          .run(
              configBuilder.buildRunConfiguration(
                  processServices(), processProjectId(new ConfigReader())));
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to run devappserver", ex);
    }
  }

  /** Run the dev appserver in async mode. */
  public void runAsync(int startSuccessTimeout) throws MojoExecutionException {
    SupportedDevServerVersion convertedVersion = convertVersionString();

    runMojo
        .getLog()
        .info(
            "Waiting "
                + startSuccessTimeout
                + " seconds for the Dev App Server "
                + runMojo.getDevserverVersion()
                + " to start.");
    try {
      runMojo
          .getAppEngineFactory()
          .devServerRunAsync(startSuccessTimeout, convertedVersion)
          .run(
              configBuilder.buildRunConfiguration(
                  processServices(), processProjectId(new ConfigReader())));
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
    runMojo.getLog().info("Dev App Server " + runMojo.getDevserverVersion() + " started.");
    runMojo.getLog().info("Use the 'mvn appengine:stop' command to stop the server.");
  }

  private Path getAppDir() {
    Build build = runMojo.getMavenProject().getBuild();
    return Paths.get(build.getDirectory()).resolve(build.getFinalName());
  }

  static final String NON_STANDARD_APPLICATION_ERROR =
      "\nCould not find appengine-web.xml all services, perhaps you need to run "
          + "'mvn package appengine:run/start'."
          + "\nDev App Server only supports appengine-web.xml based Java applications.";

  @VisibleForTesting
  List<Path> processServices() throws MojoExecutionException {
    Path appDir = getAppDir();
    List<Path> services = runMojo.getServices();
    if (services == null || services.isEmpty()) {
      return Collections.singletonList(appDir);
    }

    // verify all are appengine-web.xml based applications
    for (Path service : services) {
      if (!Files.exists(service.resolve("WEB-INF").resolve("appengine-web.xml"))) {
        throw new MojoExecutionException(NON_STANDARD_APPLICATION_ERROR);
      }
    }

    return services;
  }

  @VisibleForTesting
  String processProjectId(ConfigReader configReader) {
    String projectId = runMojo.getProjectId();
    if (projectId != null) {
      if (projectId.equals(GCLOUD_CONFIG)) {
        return configReader.getProjectId(runMojo.getAppEngineFactory().getGcloud());
      } else if (projectId.equals(APPENGINE_CONFIG)) {
        Path appengineWebXml = getAppDir().resolve("WEB-INF").resolve("appengine-web.xml");
        return configReader.getProjectId(appengineWebXml);
      }
    }
    return projectId;
  }

  /**
   * Verifies that {@code version} is of the supported values.
   *
   * @throws MojoExecutionException if {@code version} cannot be converted to {@link
   *     SupportedDevServerVersion}
   */
  private SupportedDevServerVersion convertVersionString() throws MojoExecutionException {
    try {
      return SupportedDevServerVersion.parse(runMojo.getDevserverVersion());
    } catch (IllegalArgumentException ex) {
      throw new MojoExecutionException("Invalid version", ex);
    }
  }

  static class ConfigBuilder {

    private final AbstractRunMojo runMojo;

    ConfigBuilder(AbstractRunMojo runMojo) {
      this.runMojo = runMojo;
    }

    protected RunConfiguration buildRunConfiguration(List<Path> services, String projectId) {

      return RunConfiguration.builder(services)
          .projectId(projectId)
          .host(runMojo.getHost())
          .port(runMojo.getPort())
          .adminHost(runMojo.getAdminHost())
          .adminPort(runMojo.getAdminPort())
          .additionalArguments(runMojo.getAdditionalArguments())
          .allowSkippedFiles(runMojo.getAllowSkippedFiles())
          .apiPort(runMojo.getApiPort())
          .authDomain(runMojo.getAuthDomain())
          .automaticRestart(runMojo.getAutomaticRestart())
          .clearDatastore(runMojo.getClearDatastore())
          .customEntrypoint(runMojo.getCustomEntrypoint())
          .datastorePath(runMojo.getDatastorePath())
          .defaultGcsBucketName(runMojo.getDefaultGcsBucketName())
          .devAppserverLogLevel(runMojo.getDevAppserverLogLevel())
          .environment(runMojo.getEnvironment())
          .jvmFlags(runMojo.getJvmFlags())
          .logLevel(runMojo.getLogLevel())
          .maxModuleInstances(runMojo.getMaxModuleInstances())
          .pythonStartupArgs(runMojo.getPythonStartupArgs())
          .pythonStartupScript(runMojo.getPythonStartupScript())
          .runtime(runMojo.getRuntime())
          .skipSdkUpdateCheck(runMojo.getSkipSdkUpdateCheck())
          .storagePath(runMojo.getStoragePath())
          .threadsafeOverride(runMojo.getThreadsafeOverride())
          .useMtimeFileWatcher(runMojo.getUseMtimeFileWatcher())
          .build();
    }
  }
}
