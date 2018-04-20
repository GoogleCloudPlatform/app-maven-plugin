/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

public interface AppEngineDeployer {
  void stage(StageMojo stageMojo) throws MojoExecutionException, MojoFailureException;

  void deploy(AbstractDeployMojo deployMojo) throws MojoExecutionException, MojoFailureException;

  void deployAll(AbstractDeployMojo deployMojo) throws MojoExecutionException, MojoFailureException;

  void deployCron(AbstractDeployMojo deployMojo)
      throws MojoExecutionException, MojoFailureException;

  void deployDispatch(AbstractDeployMojo deployMojo)
      throws MojoExecutionException, MojoFailureException;

  void deployDos(AbstractDeployMojo deployMojo) throws MojoExecutionException, MojoFailureException;

  void deployIndex(AbstractDeployMojo deployMojo)
      throws MojoExecutionException, MojoFailureException;

  void deployQueue(AbstractDeployMojo deployMojo)
      throws MojoExecutionException, MojoFailureException;
}
