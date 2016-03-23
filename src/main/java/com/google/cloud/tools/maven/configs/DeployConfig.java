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

import com.google.cloud.tools.maven.Deploy;

import java.util.Properties;

/**
 * Configuration for the {@link Deploy} goal.
 */
public class DeployConfig extends GcpAppGoalConfiguration {

  private static String BUCKET_KEY = "gcp.app.deploy.bucket";
  private static String DOCKER_BUILD_KEY = "gcp.app.deploy.dockerBuild";
  private static String FORCE_KEY = "gcp.app.deploy.force";
  private static String IMAGE_URL_KEY = "gcp.app.deploy.imageUrl";
  private static String PROMOTE_KEY = "gcp.app.deploy.promote";
  private static String SERVER_KEY = "gcp.app.deploy.server";
  private static String STOP_KEY = "gcp.app.deploy.stopPreviousVersion";
  private static String VERSION_KEY = "gcp.app.deploy.version";

  public String bucket;
  public String dockerBuild;
  public Boolean force;
  public String imageUrl;
  public Boolean promote;
  public String server;
  public Boolean stopPreviousVersion;
  public String version;

  public void overrideWithCommandLineFlags() {
    Properties systemProperties = System.getProperties();

    if (getStringFromMap(BUCKET_KEY, systemProperties) != null) {
      bucket = getStringFromMap(BUCKET_KEY, systemProperties);
    }
    if (getStringFromMap(DOCKER_BUILD_KEY, systemProperties) != null) {
      dockerBuild = getStringFromMap(DOCKER_BUILD_KEY, systemProperties);
    }
    if (getBooleanFromMap(FORCE_KEY, systemProperties) != null) {
      force = getBooleanFromMap(FORCE_KEY, systemProperties);
    }
    if (getStringFromMap(IMAGE_URL_KEY, systemProperties) != null) {
      imageUrl = getStringFromMap(IMAGE_URL_KEY, systemProperties);
    }
    if (getBooleanFromMap(PROMOTE_KEY, systemProperties) != null) {
      promote = getBooleanFromMap(PROMOTE_KEY, systemProperties);
    }
    if (getStringFromMap(SERVER_KEY, systemProperties) != null) {
      server = getStringFromMap(SERVER_KEY, systemProperties);
    }
    if (getBooleanFromMap(STOP_KEY, systemProperties) != null) {
      stopPreviousVersion = getBooleanFromMap(STOP_KEY, systemProperties);
    }
    if (getStringFromMap(VERSION_KEY, systemProperties) != null) {
      version = getStringFromMap(VERSION_KEY, systemProperties);
    }
  }
}
