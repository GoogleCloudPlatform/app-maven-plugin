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

import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.nio.file.Files;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates a deploy-ready application directory for App Engine standard or flexible environment
 * deployment.
 */
public abstract class AbstractStageMojo extends CloudSdkMojo
    implements StageStandardConfiguration, StageFlexibleConfiguration {

  ///////////////////////////////////
  // Standard & Flexible params
  //////////////////////////////////

  @Parameter(
    required = true,
    defaultValue = "${project.build.directory}/appengine-staging",
    alias = "stage.stagingDirectory",
    property = "app.stage.stagingDirectory"
  )
  private File stagingDirectory;

  ///////////////////////////////////
  // Standard-only params
  ///////////////////////////////////

  /**
   * The location of the dockerfile to use for App Engine Standard applications running on the
   * flexible environment.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.dockerfile", property = "app.stage.dockerfile")
  protected File dockerfile;

  /**
   * The location of the compiled web application files, or the exploded WAR. This will be used as
   * the source for staging.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(
    required = true,
    defaultValue = "${project.build.directory}/${project.build.finalName}",
    alias = "stage.sourceDirectory",
    property = "app.stage.sourceDirectory"
  )
  protected File sourceDirectory;

  /**
   * Use jetty quickstart to process servlet annotations.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableQuickstart", property = "app.stage.enableQuickstart")
  private boolean enableQuickstart;

  /**
   * Split large jar files (bigger than 10M) into smaller fragments.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarSplitting", property = "app.stage.enableJarSplitting")
  private boolean enableJarSplitting;

  /**
   * Files that match the list of comma separated SUFFIXES will be excluded from all jars.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.jarSplittingExcludes", property = "app.stage.jarSplittingExcludes")
  private String jarSplittingExcludes;

  /**
   * The character encoding to use when compiling JSPs.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.compileEncoding", property = "app.stage.compileEncoding")
  private String compileEncoding;

  /**
   * Delete the JSP source files after compilation.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.deleteJsps", property = "app.stage.deleteJsps")
  private boolean deleteJsps;

  /**
   * Do not jar the classes generated from JSPs.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.disableJarJsps", property = "app.stage.disableJarJsps")
  private boolean disableJarJsps;

  /**
   * Jar the WEB-INF/classes content.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarClasses", property = "app.stage.enableJarClasses")
  private boolean enableJarClasses;

  // always disable update check and do not expose this as a parameter
  private boolean disableUpdateCheck = true;

  // allows forcing runtime to 'java' for compat Java 8 projects
  protected String runtime;

  @Parameter(defaultValue = "${basedir}/src/main/docker/Dockerfile", readonly = true)
  protected File dockerfilePrimaryDefaultLocation;

  @Parameter(defaultValue = "${basedir}/src/main/appengine/Dockerfile", readonly = true)
  protected File dockerfileSecondaryDefaultLocation;

  ///////////////////////////////////
  // Flexible-only params
  ///////////////////////////////////

  @Parameter(alias = "stage.appEngineDirectory", property = "app.stage.appEngineDirectory")
  private File appEngineDirectory;

  /**
   * The directory containing the Dockerfile and other Docker resources.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(
    defaultValue = "${basedir}/src/main/docker/",
    alias = "stage.dockerDirectory",
    property = "app.stage.dockerDirectory"
  )
  protected File dockerDirectory;

  /**
   * The location of the JAR or WAR archive to deploy.
   *
   * <p>Applies to App Engine app.yaml based applications.
   */
  @Parameter(
    defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}",
    alias = "stage.artifact",
    property = "app.stage.artifact"
  )
  protected File artifact;

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  /** The directory to which to stage the application. */
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

  /**
   * The directory that contains app.yaml and other supported App Engine configuration files.
   *
   * <p>Applies to App Engine app.yaml based applications only. Defaults to <code>
   * ${basedir}/src/main/appengine</code>
   */
  @Override
  public File getAppEngineDirectory() {
    return appEngineDirectory;
  }

  @Override
  public File getArtifact() {
    return artifact;
  }

  @Override
  public Boolean getDisableJarJsps() {
    return disableJarJsps;
  }

  @Override
  public File getDockerDirectory() {
    return dockerDirectory;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  @VisibleForTesting
  public void setStagingDirectory(File stagingDirectory) {
    this.stagingDirectory = stagingDirectory;
  }

  @VisibleForTesting
  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public boolean isStandardStaging() {
    return Files.exists(sourceDirectory.toPath().resolve("WEB-INF").resolve("appengine-web.xml"));
  }

  public void setAppEngineDirectory(File appEngineDirectory) {
    this.appEngineDirectory = appEngineDirectory;
  }
}
