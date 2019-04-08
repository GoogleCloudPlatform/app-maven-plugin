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

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.RunConfiguration;
import com.google.cloud.tools.maven.cloudsdk.ConfigReader;
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
    public Runner newRunner(AbstractRunMojo runMojo) throws MojoExecutionException {
      for (Path service : runMojo.getServices()) {
        if (!Files.isRegularFile(service.resolve("WEB-INF").resolve("appengine-web.xml"))) {
          throw new MojoExecutionException(
              "appengine:run is only available for appengine-web.xml based projects,"
                  + " the service defined in: "
                  + service.toString()
                  + " cannot be run by the dev appserver.");
        }
      }
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
    try {
      runMojo
          .getAppEngineFactory()
          .devServerRunSync()
          .run(configBuilder.buildRunConfiguration(processServices(), processProjectId()));
    } catch (AppEngineException ex) {
      throw new MojoExecutionException("Failed to run devappserver", ex);
    }
  }

  /** Run the dev appserver in async mode. */
  public void runAsync(int startSuccessTimeout) throws MojoExecutionException {
    runMojo
        .getLog()
        .info("Waiting " + startSuccessTimeout + " seconds for the Dev App Server to start.");
    try {
      runMojo
          .getAppEngineFactory()
          .devServerRunAsync(startSuccessTimeout)
          .run(configBuilder.buildRunConfiguration(processServices(), processProjectId()));
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
    runMojo.getLog().info("Dev App Server started.");
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

  String processProjectId() {
    String projectId = runMojo.getProjectId();
    if (ConfigReader.GCLOUD_CONFIG.equals(projectId)) {
      return runMojo.getAppEngineFactory().newConfigReader().getProjectId();
    }
    return projectId;
  }

  static class ConfigBuilder {

    private final AbstractRunMojo runMojo;

    ConfigBuilder(AbstractRunMojo runMojo) {
      this.runMojo = runMojo;
    }

    protected RunConfiguration buildRunConfiguration(List<Path> services, String projectId) {

      return RunConfiguration.builder(services)
          .additionalArguments(runMojo.getAdditionalArguments())
          .automaticRestart(runMojo.getAutomaticRestart())
          .defaultGcsBucketName(runMojo.getDefaultGcsBucketName())
          .projectId(projectId)
          .environment(runMojo.getEnvironment())
          .host(runMojo.getHost())
          .jvmFlags(runMojo.getJvmFlags())
          .port(runMojo.getPort())
          .build();
    }
  }
}
