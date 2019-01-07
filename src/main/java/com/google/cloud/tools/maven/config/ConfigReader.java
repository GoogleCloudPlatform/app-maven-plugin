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

package com.google.cloud.tools.maven.config;

import com.google.cloud.tools.appengine.AppEngineDescriptor;
import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.operations.Gcloud;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.appengine.operations.cloudsdk.CloudSdkVersionFileException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.xml.sax.SAXException;

public class ConfigReader {
  /** Return gcloud config property for project, or error out if not found. */
  public String getProjectId(Gcloud gcloud) {
    try {
      String gcloudProject = gcloud.getConfig().getProject();
      if (gcloudProject == null || gcloudProject.trim().isEmpty()) {
        throw new RuntimeException("Project was not found in gcloud config");
      }
      return gcloudProject;
    } catch (CloudSdkNotFoundException
        | CloudSdkOutOfDateException
        | CloudSdkVersionFileException
        | IOException
        | ProcessHandlerException ex) {
      throw new RuntimeException("Failed to read project from gcloud config", ex);
    }
  }

  /** Return "application" tag from appengine-web.xml or error out if could not read. */
  public String getProjectId(Path appengineWebXml) {
    try {
      AppEngineDescriptor appEngineDescriptor =
          AppEngineDescriptor.parse(Files.newInputStream(appengineWebXml));
      String appengineWebXmlProject = appEngineDescriptor.getProjectId();
      if (appengineWebXmlProject == null || appengineWebXmlProject.trim().isEmpty()) {
        throw new RuntimeException("<application> was not found in appengine-web.xml");
      }
      return appengineWebXmlProject;
    } catch (IOException | SAXException | AppEngineException ex) {
      throw new RuntimeException("Failed to read project from appengine-web.xml", ex);
    }
  }

  /** Return "version" tag from appengine-web.xml or error out if could not read. */
  public String getVersion(Path appengineWebXml) {
    try {
      AppEngineDescriptor appEngineDescriptor =
          AppEngineDescriptor.parse(Files.newInputStream(appengineWebXml));
      String appengineWebXmlVersion = appEngineDescriptor.getProjectVersion();
      if (appengineWebXmlVersion == null || appengineWebXmlVersion.trim().isEmpty()) {
        throw new RuntimeException("<version> was not found in appengine-web.xml");
      }
      return appengineWebXmlVersion;
    } catch (IOException | SAXException | AppEngineException ex) {
      throw new RuntimeException("Failed to read version from appengine-web.xml", ex);
    }
  }
}
