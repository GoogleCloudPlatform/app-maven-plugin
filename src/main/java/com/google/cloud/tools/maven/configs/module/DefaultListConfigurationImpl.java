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

import com.google.cloud.tools.app.Option;
import com.google.cloud.tools.app.config.module.ListConfiguration;
import com.google.cloud.tools.maven.configs.GcpAppGoalConfiguration;
import com.google.cloud.tools.maven.module.List;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the {@link List} goal.
 */
public class DefaultListConfigurationImpl extends GcpAppGoalConfiguration
    implements ListConfiguration {

  private static String MODULES_KEY = "gcp.app.module.list.modules";
  private static String SERVER_KEY = "gcp.app.module.list.server";

  private Collection<String> modules = ImmutableList.of();
  private String server;

  public Collection<String> getModules() {
    return modules;
  }

  public Map<Option, String> getOptionalParameters() {
    Map<Option, String> optionalParameters = new HashMap<>();

    if (server != null) {
      optionalParameters.put(Option.SERVER, server);
    }

    return optionalParameters;
  }

  public void overrideWithCommandLineFlags() {
    if (getCollectionFromMap(MODULES_KEY, System.getProperties()) != null) {
      modules = getCollectionFromMap(MODULES_KEY, System.getProperties());
    }
    if (getCollectionFromMap(SERVER_KEY, System.getProperties()) != null) {
      server = getStringFromMap(SERVER_KEY, System.getProperties());
    }
  }

  public DefaultListConfigurationImpl newListConfiguration() {
    return new DefaultListConfigurationImpl();
  }

  public DefaultListConfigurationImpl modules(String... modules) {
    this.modules = Lists.newArrayList(modules);
    return this;
  }

  public DefaultListConfigurationImpl server(String server) {
    if (!Strings.isNullOrEmpty(server)) {
      this.server = server;
    }

    return this;
  }
}
