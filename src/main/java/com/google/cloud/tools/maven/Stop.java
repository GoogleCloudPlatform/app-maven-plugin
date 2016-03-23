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

import com.google.cloud.tools.app.GCloudExecutionException;
import com.google.cloud.tools.app.InvalidFlagException;
import com.google.cloud.tools.app.Option;
import com.google.cloud.tools.app.StopAction;
import com.google.common.base.Strings;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stops the local development server.
 */
@Mojo(name = "stop")
public class Stop extends GcpAppMojo {

  @Parameter(property = "gcp.app.adminHost")
  private String adminHost;
  @Parameter(property = "gcp.app.adminPort")
  private Integer adminPort;

  public void execute() throws MojoExecutionException {

    Map<Option, String> flags = new HashMap<>();
    if (!Strings.isNullOrEmpty(adminHost)) {
      flags.put(Option.ADMIN_HOST, adminHost);
    }
    if (adminPort != null) {
      flags.put(Option.ADMIN_PORT, adminPort.toString());
    }

    try {
      action = new StopAction(flags);

      this.executeAction();
    } catch (InvalidFlagException|GCloudExecutionException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }
}
