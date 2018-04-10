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

package com.google.cloud.tools.maven;

import com.google.cloud.tools.appengine.api.AppEngineException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/** Stage and deploy an application to Google App Engine standard or flexible environment. */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployMojo extends AbstractDeployMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!"war".equals(getPackaging()) && !"jar".equals(getPackaging())) {
      // https://github.com/GoogleCloudPlatform/app-maven-plugin/issues/85
      getLog().info("Deploy is only executed for war and jar modules.");
      return;
    }
    // execute stage
    super.execute();

    if (deployables.isEmpty()) {
      deployables.add(stagingDirectory);
    }

    try {
      getAppEngineFactory().deployment().deploy(this);
    } catch (AppEngineException ex) {
      throw new RuntimeException(ex);
    }
  }
}
