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
import com.google.cloud.tools.appengine.api.debug.GenRepoInfoFileConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Generates repository information files for the Stackdriver Debugger.
 */
@Mojo(name = "genRepoInfoFile")
@Execute(phase = LifecyclePhase.PREPARE_PACKAGE)
public class GenRepoInfoFileMojo extends CloudSdkMojo implements GenRepoInfoFileConfiguration {

  private static Logger logger = Logger.getLogger(GenRepoInfoFileMojo.class.getName());
  private static final String HOW_TO_FIX_MSG = "An error occurred while generating source context"
      + " files. Make sure your project is in a Git repository. To ignore source context generation"
      + " errors, use the -Dapp.genRepoInfoFile.ignoreErrors=true flag.";

  /**
   * The root directory containing the source code of the app. Expected to be contained in a git
   * repository.
   */
  @Parameter(alias = "genRepoInfoFile.sourceDirectory", defaultValue = "${project.basedir}",
      property = "app.genRepoInfoFile.sourceDirectory")
  protected File sourceDirectory;

  /**
   * Directory where the source context files will be generated.
   */
  @Parameter(alias = "genRepoInfoFile.outputDirectory",
      defaultValue = "${project.build.outputDirectory}",
      property = "app.genRepoInfoFile.outputDirectory")
  protected String outputDirectory;

  /**
   * If {@code true}, ignores errors generating the source context files and proceeds to deployment.
   * If {@code false}, the goal is aborted by generation errors.
   */
  @Parameter(alias = "genRepoInfoFile.ignoreErrors", defaultValue = "false",
      property = "app.genRepoInfoFile.ignoreErrors")
  protected boolean ignoreErrors;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      getAppEngineFactory().genRepoInfoFile().generate(this);
    } catch (AppEngineException aee) {
      if (!ignoreErrors) {
        throw new MojoExecutionException(HOW_TO_FIX_MSG, aee);
      } else {
        logger.warning(HOW_TO_FIX_MSG);
      }
    }
  }

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  @Override
  public File getOutputDirectory() {
    return Paths.get(outputDirectory).toFile();
  }
}
