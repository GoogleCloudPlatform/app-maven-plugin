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

import java.io.File;
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
public class DeployAllMojo extends DeployMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!deployables.isEmpty()) {
      getLog().warn("Ignoring configured deployables for deployAll.");
      deployables.clear();
    }

    String[] validYamls = {
      "app.yaml", "cron.yaml", "dispatch.yaml", "dos.yaml", "index.yaml", "queue.yaml"
    };
    for (String yamlName : validYamls) {
      File yaml = stagingDirectory.toPath().resolve(yamlName).toFile();
      if (yaml.exists()) {
        getLog().info("deployAll: Preparing to deploy " + yamlName);
        deployables.add(yaml);
      }
    }

    super.execute();
  }
}
