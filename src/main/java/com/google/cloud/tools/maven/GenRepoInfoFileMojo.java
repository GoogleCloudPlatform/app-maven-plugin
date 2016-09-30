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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.debug.DefaultGenRepoInfoFileConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Paths;

/**
 * Generates source context files.
 */
@Mojo(name = "genRepoInfoFile")
@Execute(phase = LifecyclePhase.PREPARE_PACKAGE)
public class GenRepoInfoFileMojo extends CloudSdkMojo {

  @Parameter(defaultValue = "${project.basedir}")
  private File sourceDirectory;

  @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDirectory")
  private String outputDirectory;

  @Parameter(defaultValue = "false", property = "ignoreSrcCtxError")
  private boolean ignoreErrors;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    DefaultGenRepoInfoFileConfiguration genConfig = new DefaultGenRepoInfoFileConfiguration();
    genConfig.setOutputDirectory(Paths.get(outputDirectory).toFile());
    genConfig.setSourceDirectory(sourceDirectory);

    try {
      getAppEngineFactory().genRepoInfoFile().generate(genConfig);
    } catch (AppEngineException aee) {
      if (!ignoreErrors) {
        throw new MojoExecutionException("An error occurred while generating source context files."
            + " To ignore source context generation errors and proceed to deployment, use the "
            + "-DignoreSrcCtxError flag.", aee);
      }
    }
  }
}
