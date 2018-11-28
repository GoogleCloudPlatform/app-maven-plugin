/*
 * Copyright 2016 Google LLC. All Rights Reserved. All Right Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.cloud.tools.maven.stage;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AbstractStageMojoTest {
  //
  // @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  //
  // @Mock private CloudSdkAppEngineFactory appengineFactory;
  //
  // @Mock private MavenProject mavenProject;
  //
  // @Mock private AppEngineArchiveStaging archiveStagingMock;
  //
  // @Mock private AppEngineStandardStaging standardStagingMock;
  //
  // @Mock private Log logMock;
  //
  // @Mock File artifact;
  //
  // @Spy File stagingDirectory = tempFolder.newFolder("staging");
  // @Spy File sourceDirectory = tempFolder.newFolder("source");
  //
  // @InjectMocks private AbstractStageMojo abstractStageMojo;
  //
  // public AbstractStageMojoTest() throws IOException {}
  //
  // @Before
  // public void configureStageMojo() throws IOException {
  //   MockitoAnnotations.initMocks(this);
  //   when(artifact.exists()).thenReturn(true);
  //   when(mavenProject.getProperties()).thenReturn(new Properties());
  //   when(mavenProject.getBasedir()).thenReturn(new File("/fake/project/base/dir"));
  // }
  //
  // @Test
  // @Parameters({"jar", "war"})
  // public void testStandardStaging(String packaging) throws Exception {
  //
  //   // wire up
  //   when(abstractStageMojo.getMavenProject().getPackaging()).thenReturn(packaging);
  //   when(appengineFactory.appengineWebXmlStaging()).thenReturn(standardStagingMock);
  //
  //   // create appengine-web.xml to mark it as standard environment
  //   File appengineWebXml = new File(tempFolder.newFolder("source", "WEB-INF"),
  // "appengine-web.xml");
  //   appengineWebXml.createNewFile();
  //   Files.asCharSink(appengineWebXml, Charsets.UTF_8)
  //       .write("<appengine-web-app></appengine-web-app>");
  //
  //   // invoke
  //   abstractStageMojo.execute();
  //
  //   // verify
  //   verify(standardStagingMock).stageStandard();
  //   verify(logMock).info(contains("standard"));
  // }
  //
  // @Test
  // @Parameters({"jar", "war"})
  // public void testFlexibleStaging(String packaging) throws Exception {
  //
  //   // wire up
  //   when(abstractStageMojo.getMavenProject().getPackaging()).thenReturn(packaging);
  //   when(appengineFactory.appYamlStaging()).thenReturn(archiveStagingMock);
  //
  //   // invoke
  //   abstractStageMojo.execute();
  //
  //   // verify
  //   verify(archiveStagingMock).stageFlexible(abstractStageMojo);
  //   verify(logMock).info(contains("App Engine app.yaml"));
  // }
  //
  // @Test
  // @Parameters
  // public void testRun_packagingIsNotJarOrWar(String packaging)
  //     throws MojoFailureException, MojoExecutionException, IOException {
  //   // wire up
  //   abstractStageMojo.setStagingDirectory(mock(File.class));
  //   when(abstractStageMojo.getMavenProject().getPackaging()).thenReturn(packaging);
  //
  //   abstractStageMojo.execute();
  //   verify(abstractStageMojo.getStagingDirectory(), never()).exists();
  // }
  //
  // @SuppressWarnings("unused") // used for testRun_packagingIsNotJarOrWar()
  // private Object[][] parametersForTestRun_packagingIsNotJarOrWar() {
  //   return new Object[][] {
  //     new Object[] {null},
  //     new Object[] {"pom"},
  //     new Object[] {"ear"},
  //     new Object[] {"rar"},
  //     new Object[] {"par"},
  //     new Object[] {"ejb"},
  //     new Object[] {"maven-plugin"},
  //     new Object[] {"eclipse-plugin"}
  //   };
  // }
}
