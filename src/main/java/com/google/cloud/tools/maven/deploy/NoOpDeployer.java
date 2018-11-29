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

package com.google.cloud.tools.maven.deploy;

import org.apache.maven.plugin.MojoExecutionException;

/** No op deployer for non-war/jar modules. */
public class NoOpDeployer implements Deployer {

  @Override
  public void deploy() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployAll() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployCron() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployDispatch() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployDos() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployIndex() throws MojoExecutionException {
    // do nothing
  }

  @Override
  public void deployQueue() throws MojoExecutionException {
    // do nothing
  }
}
