/**
 * Copyright 2016 Google Inc.
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

import com.google.cloud.tools.app.Action;
import com.google.cloud.tools.app.DeployAction;
import com.google.cloud.tools.app.GCloudExecutionException;
import com.google.cloud.tools.app.InvalidDirectoryException;
import com.google.cloud.tools.app.InvalidFlagException;
import com.google.cloud.tools.app.Option;
import com.google.cloud.tools.app.StageAction;
import com.google.cloud.tools.app.StageGenericJavaAction;
import com.google.cloud.tools.maven.configs.DeployConfig;
import com.google.cloud.tools.maven.configs.StageConfig;
import com.google.common.base.Strings;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deploys an app to the server.
 */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class Deploy extends GcpAppMojo {

  // Maven parameters.
  @Parameter(defaultValue = "${stage}")
  private StageConfig stageConfig;
  @Parameter(defaultValue = "${deploy}")
  private DeployConfig deployConfig;
  @Parameter(property = "gcp.app.deploy.stagingDir",
      defaultValue = "${project.build.directory}/appengine-staging")
  private String stagingDirLocation;
  @Parameter(property = "gcp.app.deploy.sourceDir",
      defaultValue = "${project.build.directory}/${project.build.finalName}")
  private String sourceDirLocation;
  @Parameter(property = "gcp.app.deploy.configDir",
      defaultValue = "${project.basedir}/src/main/appengine")
  private String configLocation;

  // Required by SdkResolver.
  @Component
  protected RepositorySystem repositorySystem;
  @Parameter(defaultValue = "${repositorySystemSession}")
  protected RepositorySystemSession repositorySession;
  @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  protected List<RemoteRepository> projectRepositories;
  @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
  protected List<RemoteRepository> pluginRepositories;

  // Class members.
  private File stagingDir;

  public void execute() throws MojoExecutionException {

    getLog().info("Parsing flags.");
    // Applies command line flag overrides specified with -D.
    deployConfig.overrideWithCommandLineFlags();

    getLog().info("Starting staging.");
    try {
      stagingDir = prepareStagingEnvironment();
    } catch (IOException ioEx) {
      getLog().error(ioEx);
      return;
    }

    try {
      File appEngineXml = new File(sourceDirLocation + "/WEB-INF/appengine-web.xml");
      Action stageAction;
      if (mavenProject.getPackaging().equals("jar") || !appEngineXml.exists()) {
        File configDir = new File(configLocation);
        File appYaml = new File(configDir, "app.yaml");
        File dockerfile = new File(configDir, "Dockerfile");
        File artifact = new File(new File(mavenProject.getBuild().getDirectory()),
            mavenProject.getBuild().getFinalName() + "." + mavenProject.getPackaging());

        stageAction = new StageGenericJavaAction(appYaml, dockerfile, artifact, stagingDir);
        stageAction.execute();
      } else {
        File sdkRoot = SdkResolver.getSdk(mavenProject, repositorySystem, repositorySession,
            pluginRepositories, projectRepositories);
        stageAction = new StageAction(sourceDirLocation, stagingDirLocation,
            sdkRoot.getAbsolutePath(), getStagingFlags());
        stageAction.execute();
      }
      action = new DeployAction(stagingDir.getAbsolutePath(), getFlags());

      getLog().info("Starting deployment with gcloud.");
      this.executeAction();
    } catch (InvalidDirectoryException |InvalidFlagException|GCloudExecutionException
        |IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  /**
   * Prepares the staging directory for the staging phase.
   *
   * <p>If the staging directory already exists, its content is deleted. If there is no staging
   * directory yet, it creates an empty one.
   */
  private File prepareStagingEnvironment() throws IOException, MojoExecutionException {
    File stagingDir = new File(stagingDirLocation);

    if (stagingDir.exists() && stagingDir.isFile()) {
      throw new MojoExecutionException(
          "Staging location already exists and is a file. Please delete it before deploying. "
          + "Location: " + stagingDir.getAbsolutePath());
    }

    if (!stagingDir.exists()) {
      if (!stagingDir.mkdir()) {
        throw new MojoExecutionException(
            "Staging directory could not be created at " + stagingDir.getAbsolutePath());
      }
      getLog().info("Staging directory created at " + stagingDir.getAbsolutePath());
    }

    if (stagingDir.listFiles().length > 0) {
      FileUtils.cleanDirectory(stagingDir);
    }

    return stagingDir;
  }

  private Map<Option, String> getStagingFlags() {
    Map<Option, String> flags = new HashMap<>();

    if (stageConfig.enableQuickstart != null) {
      flags.put(Option.ENABLE_QUICKSTART, stageConfig.enableQuickstart.toString());
    }
    if (stageConfig.disableUpdateCheck != null) {
      flags.put(Option.DISABLE_UPDATE_CHECK, stageConfig.disableUpdateCheck.toString());
    }
    if (!Strings.isNullOrEmpty(stageConfig.version)) {
      flags.put(Option.VERSION, stageConfig.version);
    }
    if (!Strings.isNullOrEmpty(stageConfig.gcloudProject)) {
      flags.put(Option.GCLOUD_PROJECT, stageConfig.gcloudProject);
    }
    if (stageConfig.enableJarSplitting != null) {
      flags.put(Option.ENABLE_JAR_SPLITTING, stageConfig.enableJarSplitting.toString());
    }
    if (!Strings.isNullOrEmpty(stageConfig.jarSplittingExcludes)) {
      flags.put(Option.JAR_SPLITTING_EXCLUDES, stageConfig.jarSplittingExcludes);
    }
    if (stageConfig.retainUploadDir != null) {
      flags.put(Option.RETAIN_UPLOAD_DIR, stageConfig.retainUploadDir.toString());
    }
    if (!Strings.isNullOrEmpty(stageConfig.compileEncoding)) {
      flags.put(Option.COMPILE_ENCODING, stageConfig.compileEncoding);
    }
    if (stageConfig.force != null) {
      flags.put(Option.FORCE, stageConfig.force.toString());
    }
    if (stageConfig.deleteJsps != null) {
      flags.put(Option.DELETE_JSPS, stageConfig.deleteJsps.toString());
    }
    if (stageConfig.enableJarClasses != null) {
      flags.put(Option.ENABLE_JAR_CLASSES, stageConfig.enableJarClasses.toString());
    }
    if (!Strings.isNullOrEmpty(stageConfig.runtime)) {
      flags.put(Option.RUNTIME, stageConfig.runtime);
    }

    return flags;
  }

  private Map<Option, String> getFlags() {
    Map<Option, String> flags = new HashMap<>();

    if (!Strings.isNullOrEmpty(deployConfig.bucket)) {
      flags.put(Option.BUCKET, deployConfig.bucket);
    }
    if (!Strings.isNullOrEmpty(deployConfig.dockerBuild)) {
      flags.put(Option.DOCKER_BUILD, deployConfig.dockerBuild);
    }
    if (deployConfig.force != null) {
      flags.put(Option.FORCE, deployConfig.force.toString());
    }
    if (!Strings.isNullOrEmpty(deployConfig.imageUrl)) {
      flags.put(Option.IMAGE_URL, deployConfig.imageUrl);
    }
    if (deployConfig.promote != null) {
      flags.put(Option.PROMOTE, deployConfig.promote.toString());
    }
    if (!Strings.isNullOrEmpty(deployConfig.server)) {
      flags.put(Option.SERVER, deployConfig.server);
    }
    if (!Strings.isNullOrEmpty(deployConfig.version)) {
      flags.put(Option.VERSION, deployConfig.version);
    }

    return flags;
  }
}
