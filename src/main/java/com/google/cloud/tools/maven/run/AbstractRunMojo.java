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

package com.google.cloud.tools.maven.run;

import com.google.cloud.tools.maven.cloudsdk.CloudSdkMojo;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractRunMojo extends CloudSdkMojo {

  /**
   * Path to a yaml file, or a directory containing yaml files, or a directory containing WEB-INF/
   * web.xml. Defaults to <code>${project.build.directory}/${project.build.finalName}</code>.
   */
  @Parameter(alias = "devserver.services", property = "app.devserver.services", required = true)
  private List<File> services;

  /** Host name to which application modules should bind. (default: localhost) */
  @Parameter(alias = "devserver.host", property = "app.devserver.host")
  private String host;

  /** Lowest port to which application modules should bind. (default: 8080) */
  @Parameter(alias = "devserver.port", property = "app.devserver.port")
  private Integer port;

  /**
   * Additional arguments to pass to the java command when launching an instance of the app. May be
   * specified more than once. Example: "-Xmx1024m -Xms256m" (default: None)
   */
  @Parameter(alias = "devserver.jvmFlags", property = "app.devserver.jvmFlags")
  private List<String> jvmFlags;

  /**
   * Restart instances automatically when files relevant to their module are changed. (default:
   * True)
   *
   * <p><i>Supported only for devserver version 2-alpha.</i>
   */
  @Parameter(alias = "devserver.automaticRestart", property = "app.devserver.automaticRestart")
  private Boolean automaticRestart;

  /** Default Google Cloud Storage bucket name. (default: None) */
  @Parameter(
    alias = "devserver.defaultGcsBucketName",
    property = "app.devserver.defaultGcsBucketName"
  )
  private String defaultGcsBucketName;

  /** Environment variables passed to the devappserver process. */
  @Parameter(alias = "devserver.environment", property = "app.devserver.environment")
  private Map<String, String> environment;

  /** Environment variables passed to the devappserver process. */
  @Parameter(
    alias = "devserver.additionalArguments",
    property = "app.devserver.additionalArguments"
  )
  private List<String> additionalArguments;

  /** The Google Cloud Platform project name to use for this invocation of the devserver. */
  @Parameter(alias = "devserver.projectId", property = "app.devserver.projectId")
  private String projectId;

  /** Return a list of Paths, but can return also return an empty list or null. */
  public List<Path> getServices() {
    return (services == null)
        ? null
        : services.stream().map(File::toPath).collect(Collectors.toList());
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  public Boolean getAutomaticRestart() {
    return automaticRestart;
  }

  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }

  public Map<String, String> getEnvironment() {
    return environment;
  }

  public List<String> getAdditionalArguments() {
    return additionalArguments;
  }

  public String getProjectId() {
    return projectId;
  }
}
