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

package com.google.cloud.tools.maven;

import com.google.cloud.tools.managedcloudsdk.ProgressListener;

public class NoOpProgressListener implements ProgressListener {

  @Override
  public void start(String s, long l) {}

  @Override
  public void update(long l) {}

  @Override
  public void update(String s) {}

  @Override
  public void done() {}

  @Override
  public ProgressListener newChild(long l) {
    return new NoOpProgressListener();
  }
}
