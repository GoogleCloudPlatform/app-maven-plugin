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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Starts running App Engine Development App Server asynchronously.
 */
@Mojo(name = "start")
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunAsyncMojo extends RunMojo {

  /**
   * Number of seconds to wait for the server to start. Set to 0 to not wait.
   */
  @Parameter(defaultValue = "30",
      alias = "devserver.startSuccessTimeout", property = "app.devserver.startSuccessTimeout")
  protected int startSuccessTimeout;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    verifyAppEngineStandardApp();

    getLog().info("Waiting " + startSuccessTimeout + " seconds for the Dev App Server to start.");

    getAppEngineFactory().devServerRunAsync(startSuccessTimeout).run(this);

    getLog().info("Dev App Server started.");
    getLog().info("Use the 'mvn appengine:stop' command to stop the server.");
  }

}
