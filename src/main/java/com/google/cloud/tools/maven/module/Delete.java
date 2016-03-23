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

import com.google.cloud.tools.app.module.DeleteAction;
import com.google.cloud.tools.maven.GcpAppMojo;
import com.google.cloud.tools.maven.configs.module.DeleteConfig;
import com.google.common.base.Strings;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Deletes a version of one or more modules.
 */
@Mojo(name = "module-delete")
public class Delete extends GcpAppMojo {

  @Parameter(defaultValue = "${module-delete}")
  private DeleteConfig moduleDelete;

  public void execute() throws MojoExecutionException {
    moduleDelete.overrideWithCommandLineFlags();

    if (Strings.isNullOrEmpty(moduleDelete.version)) {
      throw new MojoExecutionException("No specified version. Please specify a version in "
          + "the pom.xml file or with the -D" + moduleDelete.VERSION_KEY + " flag.");
    }

    action = DeleteAction.newDeleteAction(
        moduleDelete.version, moduleDelete.modules.toArray(new String[moduleDelete.modules.size()]))
        .setServer(moduleDelete.server);

    this.executeAction();
  }
}
