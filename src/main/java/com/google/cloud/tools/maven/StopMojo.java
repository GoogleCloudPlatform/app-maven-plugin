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

import com.google.cloud.tools.app.api.devserver.StopConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Stops a running App Engine Development App Server.
 */
@Mojo(name = "stop")
public class StopMojo extends CloudSdkMojo implements StopConfiguration {

  /**
   * Host name to which the admin server was bound. (default: localhost)
   */
  @Parameter(alias = "devserver.adminHost", property = "app.devserver.adminHost")
  protected String adminHost;

  /**
   * Port to which the admin server was bound. (default: 8000)
   */
  @Parameter(alias = "devserver.adminPort", property = "app.devserver.adminPort")
  protected Integer adminPort;

  public void execute() throws MojoExecutionException, MojoFailureException {
    factory.devServerStop().stop(this);
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }
}
