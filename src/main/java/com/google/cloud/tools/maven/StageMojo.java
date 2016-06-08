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

import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Generates a deploy-ready application directory for App Engine standard or flexible environment
 * deployment.
 */
@Mojo(name = "stage")
@Execute(phase = LifecyclePhase.PACKAGE)
public class StageMojo extends CloudSdkMojo implements StageStandardConfiguration,
    StageFlexibleConfiguration {

  ///////////////////////////////////
  // Standard & Flexible params
  //////////////////////////////////

  /**
   * The directory to which to stage the application.
   */
  @Parameter(required = true, defaultValue = "${project.build.directory}/appengine-staging",
      alias = "stage.stagingDirectory", property = "app.stage.stagingDirectory")
  protected File stagingDirectory;

  /**
   * The location of the dockerfile to use for App Engine flexible environment. This also applies to
   * App Engine Standard applications running on the flexible environment.
   */
  @Parameter(alias = "stage.dockerfile", property = "app.stage.dockerfile")
  protected File dockerfile;

  ///////////////////////////////////
  // Standard-only params
  ///////////////////////////////////

  /**
   * The location of the compiled web application files, or the exploded WAR. This will be used as
   * the source for staging.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(required = true,
      defaultValue = "${project.build.directory}/${project.build.finalName}",
      alias = "stage.sourceDirectory", property = "app.stage.sourceDirectory")
  protected File sourceDirectory;


  /**
   * Use jetty quickstart to process servlet annotations.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableQuickstart", property = "app.stage.enableQuickstart")
  protected boolean enableQuickstart;

  /**
   * Split large jar files (> 10M) into smaller fragments.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarSplitting", property = "app.stage.enableJarSplitting")
  protected boolean enableJarSplitting;

  /**
   * Files that match the list of comma separated SUFFIXES will be excluded from all jars.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.jarSplittingExcludes", property = "app.stage.jarSplittingExcludes")
  protected String jarSplittingExcludes;

  /**
   * The character encoding to use when compiling JSPs.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.compileEncoding", property = "app.stage.compileEncoding")
  protected String compileEncoding;

  /**
   * Delete the JSP source files after compilation.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.deleteJsps", property = "app.stage.deleteJsps")
  protected boolean deleteJsps;

  /**
   * Do not jar the classes generated from JSPs.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.disableJarJsps", property = "app.stage.disableJarJsps")
  protected boolean disableJarJsps;

  /**
   * Jar the WEB-INF/classes content.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarClasses", property = "app.stage.enableJarClasses")
  protected boolean enableJarClasses;

  // always disable update check and do not expose this as a parameter
  private boolean disableUpdateCheck = true;

  @Parameter(defaultValue = "${basedir}/src/main/docker/Dockerfile", readonly = true)
  private File dockerfilePrimaryDefaultLocation;

  @Parameter(defaultValue = "${basedir}/src/main/appengine/Dockerfile", readonly = true)
  private File dockerfileSecondaryDefaultLocation;

  ///////////////////////////////////
  // Flexible-only params
  ///////////////////////////////////

  /**
   * The location of the app.yaml in the source directory.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue = "${basedir}/src/main/appengine/app.yaml",
      alias = "stage.appYaml", property = "app.stage.appYaml")
  protected File appYaml;

  /**
   * The location of the JAR or WAR archive to deploy.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue =
      "${project.build.directory}/${project.build.finalName}.${project.packaging}",
      alias = "stage.artifact", property = "app.stage.artifact")
  protected File artifact;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // delete staging directory if it exists
    if (stagingDirectory.exists()) {
      getLog().info("Deleting the staging directory: " + stagingDirectory);
      try {
        FileUtils.deleteDirectory(stagingDirectory);
      } catch (IOException e) {
        throw new MojoFailureException("Unable to delete staging directory.", e);
      }
    }
    if (!stagingDirectory.mkdir()) {
      throw new MojoExecutionException("Unable to create staging directory");
    }

    // Dockerfile default location
    configureDockerfileDefaultLocation();

    getLog().info("Staging the application to: " + stagingDirectory);

    if (new File(sourceDirectory.toString() + "/WEB-INF/appengine-web.xml").exists()) {
      getLog().info("Detected App Engine standard environment application.");
      getAppEngineFactory().standardStaging().stageStandard(this);
    } else {
      getLog().info("Detected App Engine flexible environment application.");
      getAppEngineFactory().flexibleStaging().stageFlexible(this);
    }
  }

  protected void configureDockerfileDefaultLocation() {
    if (dockerfile == null) {
      if (dockerfilePrimaryDefaultLocation != null
          && dockerfilePrimaryDefaultLocation.exists()) {
        dockerfile = dockerfilePrimaryDefaultLocation;
      } else if (dockerfileSecondaryDefaultLocation != null
          && dockerfileSecondaryDefaultLocation.exists()) {
        dockerfile = dockerfileSecondaryDefaultLocation;
      }
    }
  }

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  @Override
  public Boolean getEnableQuickstart() {
    return enableQuickstart;
  }

  @Override
  public Boolean getDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  @Override
  public Boolean getEnableJarSplitting() {
    return enableJarSplitting;
  }

  @Override
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  @Override
  public String getCompileEncoding() {
    return compileEncoding;
  }

  @Override
  public Boolean getDeleteJsps() {
    return deleteJsps;
  }

  @Override
  public Boolean getEnableJarClasses() {
    return enableJarClasses;
  }

  @Override
  public File getAppYaml() {
    return appYaml;
  }

  @Override
  public File getArtifact() {
    return artifact;
  }

  @Override
  public Boolean getDisableJarJsps() {
    return disableJarJsps;
  }
}
