/*
 * Copyright (C) 2017 Google Inc.
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

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.DeployProjectConfigurationConfiguration;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stage and deploy dispatch.yaml to Google App Engine standard or flexible environment.
 */
@Mojo(name = "deployDispatch")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployDispatchMojo extends AbstractSingleYamlDeployMojo {

  protected void doDeploy(AppEngineDeployment appEngineDeployment,
      DeployProjectConfigurationConfiguration configuration) {
    appEngineDeployment.deployDispatch(this);
  }
}
