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
package com.google.cloud.tools.maven.module;

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.InvalidFlagException;
import com.google.cloud.tools.Option;
import com.google.cloud.tools.module.StartAction;
import com.google.cloud.tools.maven.GcpAppMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.HashMap;
import java.util.Map;

/**
 * Starts serving a version.
 */
@Mojo(name = "module-start")
public class Start extends GcpAppMojo {

  public void execute() throws MojoExecutionException {
    if (Strings.isNullOrEmpty(version)) {
      throw new MojoExecutionException("Version was not specified. Please specify a version in " +
      "the pom.xml file or with the -Dgcp.app.version flag.");
    }

    Map<Option, String> flags = new HashMap<>();
    if (!Strings.isNullOrEmpty(server)) {
      flags.put(Option.SERVER, server);
    }

    action = new StartAction(modules, version, flags);

    this.executeAction();
  }
}
