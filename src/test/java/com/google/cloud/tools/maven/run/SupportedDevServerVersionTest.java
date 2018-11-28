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

package com.google.cloud.tools.maven.run;

import static org.junit.Assert.assertEquals;

import com.google.cloud.tools.maven.cloudsdk.CloudSdkAppEngineFactory.SupportedDevServerVersion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SupportedDevServerVersionTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testParseV1() {
    assertEquals(SupportedDevServerVersion.V1, SupportedDevServerVersion.parse("1"));
  }

  @Test
  public void testParseV2Alpha() {
    assertEquals(SupportedDevServerVersion.V2ALPHA, SupportedDevServerVersion.parse("2-alpha"));
  }

  @Test
  public void testParseInvalidVersion() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unsupported version value: foo");

    SupportedDevServerVersion.parse("foo");
  }
}
