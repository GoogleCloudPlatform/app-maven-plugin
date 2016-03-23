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
package com.google.cloud.tools.maven.configs.module;

import com.google.cloud.tools.maven.configs.GcpAppGoalConfiguration;
import com.google.cloud.tools.maven.module.Delete;

import java.util.Collection;

/**
 * Configuration for the {@link Delete} goal.
 */
public class DeleteConfig extends GcpAppGoalConfiguration {

  public static String MODULES_KEY = "gcp.app.module.delete.modules";
  public static String VERSION_KEY = "gcp.app.module.delete.version";
  public static String SERVER_KEY = "gcp.app.module.delete.server";

  public Collection<String> modules;
  public String version;
  public String server;

  public void overrideWithCommandLineFlags() {
    if (getCollectionFromMap(MODULES_KEY, System.getProperties()) != null) {
      modules = getCollectionFromMap(MODULES_KEY, System.getProperties());
    }
    if (getStringFromMap(VERSION_KEY, System.getProperties()) != null) {
      version = getStringFromMap(VERSION_KEY, System.getProperties());
    }
    if (getStringFromMap(SERVER_KEY, System.getProperties()) != null) {
      server = getStringFromMap(SERVER_KEY, System.getProperties());
    }
  }
}
