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

package com.google.cloud.tools.maven.stage;

import com.google.cloud.tools.maven.cloudsdk.CloudSdkMojo;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractStageMojo extends CloudSdkMojo {

  ///////////////////////////////////
  // Standard & Flexible params
  //////////////////////////////////

  @Parameter(
    required = true,
    defaultValue = "${project.build.directory}/appengine-staging",
    alias = "stage.stagingDirectory",
    property = "app.stage.stagingDirectory"
  )
  File stagingDirectory;

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
  private File dockerfile;

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
  private File sourceDirectory;

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

  @Parameter(defaultValue = "${basedir}/src/main/docker/Dockerfile", readonly = true)
  private File dockerfilePrimaryDefaultLocation;

  @Parameter(defaultValue = "${basedir}/src/main/appengine/Dockerfile", readonly = true)
  private File dockerfileSecondaryDefaultLocation;

  ///////////////////////////////////
  // Flexible-only params
  ///////////////////////////////////

  @Parameter(
    defaultValue = "${basedir}/src/main/appengine",
    alias = "stage.appEngineDirectory",
    property = "app.stage.appEngineDirectory"
  )
  private File appEngineDirectory;

  @Parameter(alias = "stage.extraFilesDirectories", property = "app.stage.extraFilesDirectories")
  private List<File> extraFilesDirectories;

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
  private File dockerDirectory;

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
  private File artifact;

  public boolean isAppEngineCompatiblePackaging() {
    return ImmutableList.of("jar", "war").contains(getMavenProject().getPackaging());
  }

  public boolean isAppEngineWebXmlBased() {
    return Files.exists(getAppEngineWebXml());
  }

  public Path getAppEngineWebXml() {
    return getSourceDirectory().resolve("WEB-INF").resolve("appengine-web.xml");
  }

  public Path getStagingDirectory() {
    return stagingDirectory.toPath();
  }

  public Path getDockerfile() {
    return dockerfile == null ? null : dockerfile.toPath();
  }

  public Path getSourceDirectory() {
    return sourceDirectory.toPath();
  }

  public boolean isEnableQuickstart() {
    return enableQuickstart;
  }

  public boolean isEnableJarSplitting() {
    return enableJarSplitting;
  }

  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  public String getCompileEncoding() {
    return compileEncoding;
  }

  public boolean isDeleteJsps() {
    return deleteJsps;
  }

  public boolean isDisableJarJsps() {
    return disableJarJsps;
  }

  public boolean isEnableJarClasses() {
    return enableJarClasses;
  }

  public boolean isDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  public Path getDockerfilePrimaryDefaultLocation() {
    return dockerfilePrimaryDefaultLocation.toPath();
  }

  public Path getDockerfileSecondaryDefaultLocation() {
    return dockerfileSecondaryDefaultLocation.toPath();
  }

  public Path getAppEngineDirectory() {
    return appEngineDirectory == null ? null : appEngineDirectory.toPath();
  }

  /** Returns a nullable list of Path to user configured extra files directories. */
  public List<Path> getExtraFilesDirectories() {
    return extraFilesDirectories == null
        ? null
        : extraFilesDirectories.stream().map(File::toPath).collect(Collectors.toList());
  }

  public Path getDockerDirectory() {
    return dockerDirectory == null ? null : dockerDirectory.toPath();
  }

  public Path getArtifact() {
    return artifact == null ? null : artifact.toPath();
  }
}
