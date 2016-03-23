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
package com.google.cloud.tools.maven;

import com.google.cloud.tools.app.Option;
import com.google.common.base.Strings;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides common members and methods to the Run goals.
 */
public abstract class GcpAppRunMojo extends GcpAppMojo {

  @Parameter(property = "gcp.app.yaml",
      defaultValue = "${project.build.directory}/${project.build.finalName}/app.yaml")
  protected String appYaml;
  @Parameter(property = "gcp.app.host")
  protected String host;
  @Parameter(property = "gcp.app.port")
  protected Integer port;
  @Parameter(property = "gcp.app.adminHost")
  protected String adminHost;
  @Parameter(property = "gcp.app.adminPort")
  protected Integer adminPort;
  @Parameter(property = "gcp.app.authDomain")
  protected String authDomain;
  @Parameter(property = "gcp.app.logLevel")
  protected String logLevel;
  @Parameter(property = "gcp.app.maxModuleInstances")
  protected Integer maxModuleInstances;
  @Parameter(property = "gcp.app.useMtimeFileWatcher")
  protected Boolean useMtimeFileWatcher;
  @Parameter(property = "gcp.app.threadsafeOverride")
  protected Boolean threadsafeOverride;
  @Parameter(property = "gcp.app.pythonStartupScript")
  protected String pythonStartupScript;
  @Parameter(property = "gcp.app.pythonStartupArgs")
  protected String pythonStartupArgs;
  @Parameter(property = "gcp.app.jvmFlag")
  protected String jvmFlag;
  @Parameter(property = "gcp.app.customEntrypoint")
  protected String customEntrypoint;
  @Parameter(property = "gcp.app.runtime")
  protected String runtime;
  @Parameter(property = "gcp.app.allowSkippedFiles")
  protected Boolean allowSkippedFiles;
  @Parameter(property = "gcp.app.apiPort")
  protected Integer apiPort;
  @Parameter(property = "gcp.app.automaticRestart")
  protected Boolean automaticRestart;
  @Parameter(property = "gcp.app.devAppserverLogLevel")
  protected String devAppserverLogLevel;
  @Parameter(property = "gcp.app.skipSdkUpdateCheck")
  protected Boolean skipSdkUpdateCheck;
  @Parameter(property = "gcp.app.defaultGcsBucketName")
  protected String defaultGcsBucketName;

  // TODO(joaomartins): Handle case where app.yaml file is passed in a property as a relative
  // path?
  // TODO(joaomartins): Handle case where given app.yaml doesn't exist.

  protected Map<Option, String> getFlags() {
    Map<Option, String> flags = new HashMap<>();

    if (!Strings.isNullOrEmpty(host)) {
      flags.put(Option.HOST, host);
    }
    if (port != null) {
      flags.put(Option.PORT, port.toString());
    }
    if (!Strings.isNullOrEmpty(adminHost)) {
      flags.put(Option.ADMIN_HOST, adminHost);
    }
    if (adminPort != null) {
      flags.put(Option.ADMIN_PORT, adminPort.toString());
    }
    if (!Strings.isNullOrEmpty(authDomain)) {
      flags.put(Option.AUTH_DOMAIN, authDomain);
    }
    if (!Strings.isNullOrEmpty(logLevel)) {
      flags.put(Option.LOG_LEVEL, logLevel);
    }
    if (maxModuleInstances != null) {
      flags.put(Option.MAX_MODULE_INSTANCES, maxModuleInstances.toString());
    }
    if (useMtimeFileWatcher != null) {
      flags.put(Option.USE_MTIME_FILE_WATCHER, useMtimeFileWatcher.toString());
    }
    if (threadsafeOverride != null) {
      flags.put(Option.THREADSAFE_OVERRIDE, threadsafeOverride.toString());
    }
    if (!Strings.isNullOrEmpty(pythonStartupScript)) {
      flags.put(Option.PYTHON_STARTUP_SCRIPT, pythonStartupScript);
    }
    if (!Strings.isNullOrEmpty(pythonStartupArgs)) {
      flags.put(Option.PYTHON_STARTUP_ARGS, pythonStartupArgs);
    }
    if (!Strings.isNullOrEmpty(jvmFlag)) {
      flags.put(Option.JVM_FLAG, jvmFlag);
    }
    if (!Strings.isNullOrEmpty(customEntrypoint)) {
      flags.put(Option.CUSTOM_ENTRYPOINT, customEntrypoint);
    }
    if (!Strings.isNullOrEmpty(runtime)) {
      flags.put(Option.RUNTIME, runtime);
    }
    if (allowSkippedFiles != null) {
      flags.put(Option.ALLOW_SKIPPED_FILES, allowSkippedFiles.toString());
    }
    if (apiPort != null) {
      flags.put(Option.API_PORT, apiPort.toString());
    }
    if (automaticRestart != null) {
      flags.put(Option.AUTOMATIC_RESTART, automaticRestart.toString());
    }
    if (!Strings.isNullOrEmpty(devAppserverLogLevel)) {
      flags.put(Option.DEV_APPSERVER_LOG_LEVEL, devAppserverLogLevel);
    }
    if (skipSdkUpdateCheck != null) {
      flags.put(Option.SKIP_SDK_UPDATE_CHECK, skipSdkUpdateCheck.toString());
    }
    if (!Strings.isNullOrEmpty(defaultGcsBucketName)) {
      flags.put(Option.DEFAULT_GCS_BUCKET_NAME, defaultGcsBucketName);
    }
    if (!Strings.isNullOrEmpty(server)) {
      flags.put(Option.SERVER, server);
    }

    return flags;
  }
}
