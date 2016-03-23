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

import com.google.cloud.tools.app.InvalidFlagException;
import com.google.cloud.tools.app.RunAction;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Provides the run-async Maven goal, which runs the local development server asynchronously.
 */
@Mojo(name = "run-async")
public class RunAsync extends GcpAppRunMojo {

  public void execute() throws MojoExecutionException {
    try {
      action = new RunAction(appYaml, false, getFlags());

      this.executeAction();
    } catch (InvalidFlagException ife) {
      throw new MojoExecutionException(ife.getMessage(), ife);
    }
  }
}
