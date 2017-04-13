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
import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
   * Split large jar files (bigger than 10M) into smaller fragments.
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

  // allows forcing runtime to 'java' for compat Java 8 projects
  private String runtime;

  @Parameter(defaultValue = "${basedir}/src/main/docker/Dockerfile", readonly = true)
  private File dockerfilePrimaryDefaultLocation;

  @Parameter(defaultValue = "${basedir}/src/main/appengine/Dockerfile", readonly = true)
  private File dockerfileSecondaryDefaultLocation;

  ///////////////////////////////////
  // Flexible-only params
  ///////////////////////////////////

  /**
   * The directory that contains app.yaml and other supported App Engine configuration files.
   *
   * <p>Applies to App Engine flexible environment only. Defaults to
   * <code>${basedir}/src/main/appengine</code>
   */
  @Parameter(alias = "stage.appEngineDirectory", property = "app.stage.appEngineDirectory")
  protected File appEngineDirectory;

  /**
   * The directory containing the Dockerfile and other Docker resources.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue = "${basedir}/src/main/docker/",
      alias = "stage.dockerDirectory", property = "app.stage.dockerDirectory")
  protected File dockerDirectory;


  /**
   * The location of the JAR or WAR archive to deploy.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue =
      "${project.build.directory}/${project.build.finalName}.${project.packaging}",
      alias = "stage.artifact", property = "app.stage.artifact")
  protected File artifact;

  private AppEngineWebXml appengineWebXml;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!"war".equals(getPackaging()) && !"jar".equals(getPackaging())) {
      // https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/85
      getLog().info("Staging is only executed for war and jar modules.");
      return;
    }
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

    getLog().info("Staging the application to: " + stagingDirectory);

    appengineWebXml =
        new AppEngineWebXml(new File(sourceDirectory.toString() + "/WEB-INF/appengine-web.xml"));

    configureAppEngineDirectory();

    if (isStandardStaging()) {
      getLog().info("Detected App Engine standard environment application.");

      // force runtime to 'java' for compat projects using Java version >1.7
      if (Float.parseFloat(getCompileTargetVersion()) > 1.7f && appengineWebXml.isVm()) {
        runtime = "java";
      }

      // Dockerfile default location
      configureDockerfileDefaultLocation();

      getAppEngineFactory().standardStaging().stageStandard(this);
    } else {
      getLog().info("Detected App Engine flexible environment application.");
      getAppEngineFactory().flexibleStaging().stageFlexible(this);
    }
  }

  protected boolean isStandardStaging() {
    return appengineWebXml.exists();
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

  /**
   * Sets {@link #appEngineDirectory} to <code>${basedir}/src/main/appengine</code>.
   *
   * <p>Called during {@link #execute()}, subclasses can override to provide a different value
   * for {@link #appEngineDirectory}.
   */
  protected void configureAppEngineDirectory() {
    if (appEngineDirectory == null) {
      appEngineDirectory =
          mavenProject.getBasedir().toPath().resolve("src/main/appengine").toFile();
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


  /**
   * Simple parser for appengine-web.xml, this should ideally not exist, but we need it to correctly
   * error when vm=false and the user is using java8 as the target platform
   */
  private static class AppEngineWebXml {

    private final Document document;

    private AppEngineWebXml(File appengineWebXml) throws MojoExecutionException {
      try {
        if (appengineWebXml.exists()) {
          document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
              .parse(appengineWebXml);
        } else {
          document = null;
        }
      } catch (SAXException | IOException | ParserConfigurationException e) {
        throw new MojoExecutionException("Failed to parse appengine-web.xml", e);
      }
    }

    public static AppEngineWebXml parse(File appengineWebXml) throws MojoExecutionException {
      return new AppEngineWebXml(appengineWebXml);
    }

    public boolean exists() {
      return document != null;
    }

    public boolean isVm() throws MojoExecutionException {
      try {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "/appengine-web-app/vm/text()='true'";
        return (Boolean) xpath.evaluate(expression, document, XPathConstants.BOOLEAN);
      } catch (XPathExpressionException e) {
        throw new MojoExecutionException("XPath evaluation failed on appengine-web.xml", e);
      }
    }
  }

  @VisibleForTesting
  public void setStagingDirectory(File stagingDirectory) {
    this.stagingDirectory = stagingDirectory;
  }

  @VisibleForTesting
  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }
}
