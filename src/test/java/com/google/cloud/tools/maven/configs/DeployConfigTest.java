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
package com.google.cloud.tools.maven.configs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/**
 * Unit tests for {@link DeployConfig}.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeployConfigTest {

  @After
  public void tearDown() {
    System.clearProperty("gcp.app.deploy.bucket");
    System.clearProperty("gcp.app.deploy.dockerBuild");
    System.clearProperty("gcp.app.deploy.force");
    System.clearProperty("gcp.app.deploy.imageUrl");
    System.clearProperty("gcp.app.deploy.promote");
    System.clearProperty("gcp.app.deploy.server");
    System.clearProperty("gcp.app.deploy.stopPreviousVersion");
    System.clearProperty("gcp.app.deploy.version");
  }

  @Test
  public void testAllArgs() {
    DeployConfig config = new DeployConfig();
    config.bucket = "gs://bucket";
    config.dockerBuild = "remote";
    config.force = false;
    config.imageUrl = "gcr.io/google_appengine/openjdk8";
    config.promote = true;
    config.server = "appengine.google.com";
    config.stopPreviousVersion = false;
    config.version = "v1";

    System.setProperty("gcp.app.deploy.bucket", "gs://otherbucket");
    System.setProperty("gcp.app.deploy.dockerBuild", "local");
    System.setProperty("gcp.app.deploy.force", "false");
    System.setProperty("gcp.app.deploy.imageUrl", "gcr.io/google_appengine/jetty9-compat");
    System.setProperty("gcp.app.deploy.promote", "false");
    System.setProperty("gcp.app.deploy.server", "another.server.com");
    System.setProperty("gcp.app.deploy.stopPreviousVersion", "true");
    System.setProperty("gcp.app.deploy.version", "v2");

    config.overrideWithCommandLineFlags();

    assertEquals("gs://otherbucket", config.bucket);
    assertEquals("local", config.dockerBuild);
    assertFalse(config.force);
    assertEquals("gcr.io/google_appengine/jetty9-compat", config.imageUrl);
    assertFalse(config.promote);
    assertEquals("another.server.com", config.server);
    assertTrue(config.stopPreviousVersion);
    assertEquals("v2", config.version);
  }

  @Test
  public void testNoArgs() {
    DeployConfig config = new DeployConfig();

    config.overrideWithCommandLineFlags();

    assertNull(config.bucket);
    assertNull(config.dockerBuild);
    assertNull(config.force);
    assertNull(config.imageUrl);
    assertNull(config.promote);
    assertNull(config.server);
    assertNull(config.stopPreviousVersion);
    assertNull(config.version);
  }

  @Test
  public void testSomeArgs() {
    DeployConfig config = new DeployConfig();
    config.bucket = "gs://bucket";
    config.imageUrl = "gcr.io/google_appengine/openjdk8";
    config.stopPreviousVersion = false;

    System.setProperty("gcp.app.deploy.imageUrl", "gcr.io/google_appengine/jetty9-compat");
    System.setProperty("gcp.app.deploy.force", "true");

    config.overrideWithCommandLineFlags();

    assertEquals("gs://bucket", config.bucket);
    assertNull(config.dockerBuild);
    assertTrue(config.force);
    assertEquals("gcr.io/google_appengine/jetty9-compat", config.imageUrl);
    assertNull(config.promote);
    assertNull(config.server);
    assertFalse(config.stopPreviousVersion);
    assertNull(config.version);
  }
}
