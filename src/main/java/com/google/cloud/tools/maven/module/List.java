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

import com.google.cloud.tools.app.module.ListAction;
import com.google.cloud.tools.maven.GcpAppMojo;
import com.google.cloud.tools.maven.configs.module.DefaultListConfigurationImpl;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Lists versions for every module, or for a selected set of modules.
 */
@Mojo(name = "module-list")
public class List extends GcpAppMojo {


  @Parameter
  private DefaultListConfigurationImpl moduleList;

  public void execute() throws MojoExecutionException {
    // TODO(joaomartins): On no module provided by the user, this will be set to "default", whereas
    // we want to list every module.
    action = new ListAction(moduleList);

    this.executeAction();
  }
}
