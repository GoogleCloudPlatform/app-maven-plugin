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

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.StopConfiguration;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.maven.cloudsdk.CloudSdkMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Stops a running App Engine Development App Server. */
@Mojo(name = "stop")
public class StopMojo extends CloudSdkMojo {

  /**
   * Version of the dev app server to use to run the services. Supported values are "1" and
   * "2-alpha". (default: "1")
   */
  @Parameter(
    alias = "devserver.version",
    property = "app.devserver.version",
    required = true,
    defaultValue = "1"
  )
  protected String devserverVersion;

  /** Host name to which application modules should bind. (default: localhost) */
  @Parameter(alias = "devserver.host", property = "app.devserver.host")
  protected String host;

  /** Lowest port to which application modules should bind. (default: 8080) */
  @Parameter(alias = "devserver.port", property = "app.devserver.port")
  protected Integer port;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      getAppEngineFactory().devServerStop().stop(buildStopConfiguration());
    } catch (CloudSdkNotFoundException ex) {
      throw new MojoExecutionException("Stop failed", ex);
    } catch (AppEngineException ex) {
      getLog().error("Failed to stop server: " + ex.getMessage());
    }
  }

  private StopConfiguration buildStopConfiguration() {
    return StopConfiguration.builder().host(host).port(port).build();
  }
}
