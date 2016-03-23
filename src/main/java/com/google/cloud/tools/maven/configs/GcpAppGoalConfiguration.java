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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Set of configurations for a Maven goal.
 */
public abstract class GcpAppGoalConfiguration {

  public abstract void overrideWithCommandLineFlags();

  protected String getStringFromMap(String key, Map<Object, Object> map) {
    if (map.containsKey(key)) {
      return String.valueOf(map.get(key));
    }

    return null;
  }

  protected Boolean getBooleanFromMap(String key, Map<Object, Object> map) {
    if (map.containsKey(key)) {
      if (map.get(key) == null
          || String.valueOf(map.get(key)).toLowerCase(Locale.ENGLISH)
          .equals("true".toLowerCase(Locale.ENGLISH))) {
        return true;
      } else {
        return false;
      }
    }

    return null;
  }

  /**
   * Splits the value using comma as delimiter.
   */
  protected Collection<String> getCollectionFromMap(String key, Map<Object, Object> map) {
    if (map.containsKey(key)) {
      String[] tokens = String.valueOf(map.get(key)).split(",");
      return Lists.newArrayList(tokens);
    }

    return null;
  }
}
