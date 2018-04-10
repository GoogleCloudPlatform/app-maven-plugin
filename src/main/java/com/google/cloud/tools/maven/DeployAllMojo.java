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

package com.google.cloud.tools.maven;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stage and deploy the application and all configs to Google App Engine standard or flexible
 * environment.
 */
@Mojo(name = "deployAll")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployAllMojo extends AbstractDeployMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!"war".equals(getPackaging()) && !"jar".equals(getPackaging())) {
      getLog().info("deployAll is only executed for war and jar modules.");
      return;
    }

    // execute stage
    super.execute();

    doDeployAll();
  }

  /** Performs the deployAll goal using the staged directory */
  @VisibleForTesting
  public void doDeployAll() throws MojoExecutionException {
    if (!deployables.isEmpty()) {
      getLog().warn("Ignoring configured deployables for deployAll.");
      deployables.clear();
    }

    configureAppEngineDirectory();

    // Look for app.yaml
    File appYaml = stagingDirectory.toPath().resolve("app.yaml").toFile();
    if (!isStandardStaging() && !appYaml.exists()) {
      appYaml = appEngineDirectory.toPath().resolve("app.yaml").toFile();
    }
    if (!appYaml.exists()) {
      throw new MojoExecutionException("Failed to deploy all: could not find app.yaml.");
    }

    getLog().info("deployAll: Preparing to deploy app.yaml");
    deployables.add(appYaml);

    // Look for config yamls
    String[] configYamls = {"cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"};
    Path configPath =
        isStandardStaging()
            ? stagingDirectory.toPath().resolve("WEB-INF").resolve("appengine-generated")
            : appEngineDirectory.toPath();
    for (String yamlName : configYamls) {
      File yaml = configPath.resolve(yamlName).toFile();
      if (yaml.exists()) {
        getLog().info("deployAll: Preparing to deploy " + yamlName);
        deployables.add(yaml);
      }
    }

    try {
      getAppEngineFactory().deployment().deploy(this);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }
}
