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

import com.google.cloud.tools.app.Option;
import com.google.cloud.tools.app.module.SetManagedByAction;
import com.google.cloud.tools.maven.GcpAppMojo;
import com.google.common.base.Strings;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets an instance or a module as managed by google or self.
 */
@Mojo(name = "module-set-managed-by")
public class SetManagedBy extends GcpAppMojo {

  @Parameter(property = "gcp.app.managedBy")
  private String managedBy;
  @Parameter(property = "gcp.app.instance")
  private String instance;

  public void execute() throws MojoExecutionException {
    if (Strings.isNullOrEmpty(managedBy)) {
      throw new MojoExecutionException("gcp.app.managedBy parameter not specified. Use the "
          + "-Dgcp.app.managedBy flag when invoking Maven to set it.");
    }

    if (Strings.isNullOrEmpty(version)) {
      throw new MojoExecutionException("No specified version. Please specify a version in "
          + "the pom.xml file or with the -Dgcp.app.version flag.");
    }

    Option manager;

    if (managedBy.equals("google")) {
      manager = Option.GOOGLE;
    } else if (managedBy.equals("self")) {
      manager = Option.SELF;
    } else {
      throw new MojoExecutionException("gcp.app.managedBy parameter must be set to either "
          + "'google' or 'self'.");
    }

    Map<Option, String> flags = new HashMap<>();
    if (!Strings.isNullOrEmpty(server)) {
      flags.put(Option.SERVER, server);
    }
    if (!Strings.isNullOrEmpty(instance)) {
      flags.put(Option.INSTANCE, instance);
    }

    action = new SetManagedByAction(modules, version, manager, flags);

    this.executeAction();
  }
}
