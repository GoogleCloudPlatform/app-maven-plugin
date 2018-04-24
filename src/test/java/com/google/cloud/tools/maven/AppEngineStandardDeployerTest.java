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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

@RunWith(JUnitParamsRunner.class)
public class AppEngineStandardDeployerTest {
  private static final String PROJECT_BUILD = "project-build";
  private static final String PROJECT_XML = "project-xml";
  private static final String VERSION_BUILD = "version-build";
  private static final String VERSION_XML = "version-xml";

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  private File appengineWebXml;

  private AbstractDeployMojo deployMojo;
  private AppEngineStandardDeployer appEngineStandardDeployer;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    System.clearProperty("deploy.read.appengine.web.xml");
    deployMojo = new DeployMojo();
    deployMojo.sourceDirectory = tempFolder.newFolder("source");
    appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"), "appengine-web.xml");
    appEngineStandardDeployer = new AppEngineStandardDeployer(deployMojo);
  }

  @After
  public void cleanup() {
    System.clearProperty("deploy.read.appengine.web.xml");
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_buildConfig()
      throws AppEngineException, SAXException, IOException {
    createAppEngineWebXml(true);
    deployMojo.version = VERSION_BUILD;
    deployMojo.project = PROJECT_BUILD;
    appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
    Assert.assertEquals(VERSION_BUILD, deployMojo.getVersion());
    Assert.assertEquals(PROJECT_BUILD, deployMojo.getProject());
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_xml()
      throws AppEngineException, SAXException, IOException {
    System.setProperty("deploy.read.appengine.web.xml", "true");
    createAppEngineWebXml(true);
    appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
    Assert.assertEquals(VERSION_XML, deployMojo.getVersion());
    Assert.assertEquals(PROJECT_XML, deployMojo.getProject());
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_projectNotSet()
      throws IOException, AppEngineException, SAXException {
    createAppEngineWebXml(false);
    deployMojo.version = VERSION_BUILD;
    try {
      appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          "appengine-plugin does not use gcloud global project state. Please configure the "
              + "application ID in your pom.xml or appengine-web.xml.",
          ex.getMessage());
    }
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_versionNotSet()
      throws IOException, AppEngineException, SAXException {
    createAppEngineWebXml(false);
    deployMojo.project = PROJECT_BUILD;
    appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
    Assert.assertEquals(null, deployMojo.getVersion());
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_sysPropertyBothSet()
      throws AppEngineException, SAXException, IOException {
    System.setProperty("deploy.read.appengine.web.xml", "true");
    createAppEngineWebXml(true);
    deployMojo.version = VERSION_BUILD;
    deployMojo.project = PROJECT_BUILD;
    try {
      appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          "Cannot override appengine.deploy config with appengine-web.xml. Either remove "
              + "the project/version properties from your pom.xml, or clear the "
              + "deploy.read.appengine.web.xml system property to read from pom.xml.",
          ex.getMessage());
    }
  }

  @Test
  public void testUpdatePropertiesFromAppEngineWebXml_noSysPropertyOnlyXml()
      throws AppEngineException, SAXException, IOException {
    createAppEngineWebXml(true);
    try {
      appEngineStandardDeployer.updatePropertiesFromAppEngineWebXml();
      Assert.fail();
    } catch (RuntimeException ex) {
      Assert.assertEquals(
          "Project/version is set in application-web.xml, but deploy.read.appengine.web.xml is "
              + "false. If you would like to use the state from appengine-web.xml, please set the "
              + "system property deploy.read.appengine.web.xml=true.",
          ex.getMessage());
    }
  }

  private void createAppEngineWebXml(boolean withParams) throws IOException {
    appengineWebXml.createNewFile();
    Files.write(
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + (withParams
                ? "<application>"
                    + PROJECT_XML
                    + "</application><version>"
                    + VERSION_XML
                    + "</version>"
                : "")
            + "</appengine-web-app>",
        appengineWebXml,
        Charsets.UTF_8);
  }
}
