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

import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.maven.config.AppEngineWebXmlConfigProcessor;
import com.google.cloud.tools.maven.config.AppYamlConfigProcessor;
import com.google.cloud.tools.maven.config.ConfigProcessor;
import com.google.cloud.tools.maven.config.ConfigReader;
import com.google.cloud.tools.maven.deploy.AppDeployer.ConfigBuilder;
import com.google.cloud.tools.maven.stage.AppEngineWebXmlStager;
import com.google.cloud.tools.maven.stage.AppYamlStager;
import com.google.cloud.tools.maven.stage.Stager;
import java.nio.file.Files;
import org.apache.maven.plugin.MojoExecutionException;

public interface Deployer {

  class Factory {
    Deployer newDeployer(AbstractDeployMojo deployMojo) throws MojoExecutionException {
      if (!deployMojo.isAppEngineCompatiblePackaging()) {
        return new NoOpDeployer();
      }
      if (deployMojo.getArtifact() == null || !Files.exists(deployMojo.getArtifact())) {
        throw new MojoExecutionException(
            "\nCould not determine appengine environment, did you package your application?"
                + "\nRun 'mvn package appengine:deploy'");
      }
      Gcloud gcloud = deployMojo.getAppEngineFactory().getGcloud();
      if (deployMojo.isAppEngineWebXmlBased()) {
        // deployments using appengine-web.xml
        Stager stager = AppEngineWebXmlStager.newAppEngineWebXmlStager(deployMojo);
        ConfigProcessor configProcessor =
            new AppEngineWebXmlConfigProcessor(
                deployMojo.getAppEngineWebXml(), gcloud, new ConfigReader());
        ConfigBuilder configBuilder = new ConfigBuilder(deployMojo, configProcessor);
        return new AppDeployer(deployMojo, stager, configProcessor, configBuilder);
      } else {
        // deployments using app.yaml
        Stager stager = AppYamlStager.newAppYamlStager(deployMojo);
        ConfigProcessor configProcessor = new AppYamlConfigProcessor(gcloud, new ConfigReader());
        ConfigBuilder configBuilder = new ConfigBuilder(deployMojo, configProcessor);
        return new AppDeployer(deployMojo, stager, configProcessor, configBuilder);
      }
    }
  }

  void deploy() throws MojoExecutionException;

  void deployAll() throws MojoExecutionException;

  void deployCron() throws MojoExecutionException;

  void deployDispatch() throws MojoExecutionException;

  void deployDos() throws MojoExecutionException;

  void deployIndex() throws MojoExecutionException;

  void deployQueue() throws MojoExecutionException;
}
