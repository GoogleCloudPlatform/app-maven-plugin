/*
 * Copyright (C) 2016 Google Inc.
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

package com.google.cloud.tools.maven.it.verifier;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.IOException;

/**
 * Verifier that uses a test App Engine Standard project.
 */
public class StandardVerifier extends TailingVerifier {

  /**
   * Creates the verifier with the given name and loads the project
   * {@code /projects/standard-project} as a resource using the provided {@link Class}.
   */
  public StandardVerifier(String testName, Class<?> clazz) throws IOException,
      VerificationException {
    super(testName,
        ResourceExtractor
            .simpleExtractResources(clazz, "/projects/standard-project")
            .getAbsolutePath());
  }
}
