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
package com.google.cloud.tools.maven.module;

import com.google.cloud.tools.app.Option;
import com.google.cloud.tools.app.module.GetLogsAction;
import com.google.cloud.tools.maven.GcpAppMojo;
import com.google.common.base.Strings;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Gets logs for a version.
 */
@Mojo(name = "module-get-logs")
public class GetLogs extends GcpAppMojo {

  @Parameter(name = "append", property = "gcp.app.append")
  private String appendFile;
  @Parameter(property = "gcp.app.days")
  private Integer days;
  @Parameter(property = "gcp.app.details")
  private Boolean details;
  @Parameter(property = "gcp.app.endDate")
  private String endDate;
  @Parameter(property = "gcp.app.severity")
  private String severity;
  @Parameter(property = "gcp.app.vhost")
  private String vhost;

  // TODO(joaomartins): Create temp file so that logs are never printed to stdout.
  @Parameter(property = "gcp.app.logfile", defaultValue = "${project.build.directory}/logfile")
  private String logFileLocation;

  public void execute() throws MojoExecutionException {
    if (Strings.isNullOrEmpty(version)) {
      throw new MojoExecutionException("No specified version. Please specify a version in "
          + "the pom.xml file or with the -Dgcp.app.version flag.");
    }

    getLog().info("Logs will be saved at " + logFileLocation);

    action = GetLogsAction.newGetLogsAction()
        .setModules(modules)
        .setVersion(version)
        .setLogFileLocation(logFileLocation)
        .setAppend(appendFile)
        .setDays(days.toString())
        .setDetails(details)
        .setEndDate(endDate)
        .setServer(server)
        .setSeverity(severity)
        .setVhost(vhost);

    this.executeAction();
  }

  private Map<Option, String> getFlags() {
    Map<Option, String> flags = new HashMap<>();

    if (!Strings.isNullOrEmpty(appendFile)) {
      flags.put(Option.APPEND, appendFile);
    }
    if (days != null) {
      flags.put(Option.DAYS, days.toString());
    }
    if (details != null) {
      flags.put(Option.DETAILS, details.toString());
    }
    if (!Strings.isNullOrEmpty(endDate)) {
      flags.put(Option.END_DATE, endDate);
    }
    if (!Strings.isNullOrEmpty(severity)) {
      flags.put(Option.SEVERITY, severity);
    }
    if (!Strings.isNullOrEmpty(vhost)) {
      flags.put(Option.VHOST, vhost);
    }
    if (!Strings.isNullOrEmpty(server)) {
      flags.put(Option.SERVER, server);
    }
    return flags;
  }
}
