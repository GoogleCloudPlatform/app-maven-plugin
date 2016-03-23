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

import java.util.Properties;

/**
 * Configuration for the {@link com.google.cloud.tools.StageAction} goal.
 */
public class StageConfig extends GcpAppGoalConfiguration {

  private static String ENABLE_QUICKSTART_KEY = "gcp.app.stage.enable-quickstart";
  private static String DISABLE_UPDATE_CHECK_KEY = "gcp.app.stage.disable-update-check";
  private static String VERSION_KEY = "gcp.app.stage.version";
  private static String GCLOUD_PROJECT_KEY = "gcp.app.stage.project";
  private static String ENABLE_JAR_SPLITTING_KEY = "gcp.app.stage.enable-jar-splitting";
  private static String JAR_SPLITTING_EXCLUDES_KEY = "gcp.app.stage.jar-splitting-excludes";
  private static String RETAIN_UPLOAD_DIR_KEY = "gcp.app.stage.retain-upload-dir";
  private static String COMPILE_ENCODING_KEY = "gcp.app.stage.compile-encoding-key";
  private static String FORCE_KEY = "gcp.app.stage.force";
  private static String DELETE_JSPS_KEY = "gcp.app.stage.delete-jsps";
  private static String ENABLE_JAR_CLASSES_KEY = "gcp.app.stage.enable-jar-classes";
  private static String RUNTIME_KEY = "gcp.app.stage.runtime";

  public Boolean enableQuickstart;
  public Boolean disableUpdateCheck;
  public String version;
  public String gcloudProject;
  public Boolean enableJarSplitting;
  public String jarSplittingExcludes;
  public Boolean retainUploadDir;
  public String compileEncoding;
  public Boolean force;
  public Boolean deleteJsps;
  public Boolean enableJarClasses;
  public String runtime;

  public void overrideWithCommandLineFlags() {
    Properties systemProperties = System.getProperties();

    if (getBooleanFromMap(ENABLE_QUICKSTART_KEY, systemProperties) != null) {
      enableQuickstart = getBooleanFromMap(ENABLE_QUICKSTART_KEY, systemProperties);
    }
    if (getBooleanFromMap(DISABLE_UPDATE_CHECK_KEY, systemProperties) != null) {
      disableUpdateCheck = getBooleanFromMap(DISABLE_UPDATE_CHECK_KEY, systemProperties);
    }
    if (getStringFromMap(VERSION_KEY, systemProperties) != null) {
      version = getStringFromMap(VERSION_KEY, systemProperties);
    }
    if (getStringFromMap(GCLOUD_PROJECT_KEY, systemProperties) != null) {
      gcloudProject = getStringFromMap(GCLOUD_PROJECT_KEY, systemProperties);
    }
    if (getBooleanFromMap(ENABLE_JAR_SPLITTING_KEY, systemProperties) != null) {
      enableJarSplitting = getBooleanFromMap(ENABLE_JAR_SPLITTING_KEY, systemProperties);
    }
    if (getStringFromMap(JAR_SPLITTING_EXCLUDES_KEY, systemProperties) != null) {
      jarSplittingExcludes = getStringFromMap(JAR_SPLITTING_EXCLUDES_KEY, systemProperties);
    }
    if (getBooleanFromMap(RETAIN_UPLOAD_DIR_KEY, systemProperties) != null) {
      retainUploadDir = getBooleanFromMap(RETAIN_UPLOAD_DIR_KEY, systemProperties);
    }
    if (getStringFromMap(COMPILE_ENCODING_KEY, systemProperties) != null) {
      compileEncoding = getStringFromMap(COMPILE_ENCODING_KEY, systemProperties);
    }
    if (getBooleanFromMap(FORCE_KEY, systemProperties) != null) {
      force = getBooleanFromMap(FORCE_KEY, systemProperties);
    }
    if (getBooleanFromMap(DELETE_JSPS_KEY, systemProperties) != null) {
      deleteJsps = getBooleanFromMap(DELETE_JSPS_KEY, systemProperties);
    }
    if (getBooleanFromMap(ENABLE_JAR_CLASSES_KEY, systemProperties) != null) {
      enableJarClasses = getBooleanFromMap(ENABLE_JAR_CLASSES_KEY, systemProperties);
    }
    if (getStringFromMap(RUNTIME_KEY, systemProperties) != null) {
      runtime = getStringFromMap(RUNTIME_KEY, systemProperties);
    }
  }
}
