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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRunMojoTest {
  private static class AbstractRunMojoImpl extends AbstractRunMojo {
    @Override
    public void execute() {
      // do nothing;
    }

    public void injectServices(List<File> services)
        throws NoSuchFieldException, IllegalAccessException {
      Field servicesField = AbstractRunMojo.class.getDeclaredField("services");
      servicesField.setAccessible(true);
      servicesField.set(this, services);
    }
  }

  @Mock private MavenProject mavenProject;

  @InjectMocks private final AbstractRunMojoImpl testMojo = new AbstractRunMojoImpl();

  @Before
  public void setUp() {
    Build build = Mockito.mock(Build.class);
    MavenProject copy = new MavenProject();
    copy.setBuild(build);
    Mockito.when(mavenProject.clone()).thenReturn(copy);
    Mockito.when(build.getDirectory()).thenReturn("fake-build-dir");
    Mockito.when(build.getFinalName()).thenReturn("fake-final-name");
  }

  @Test
  public void testGetServices_null() throws NoSuchFieldException, IllegalAccessException {
    testMojo.injectServices(null);
    List<Path> expected = ImmutableList.of(Paths.get("fake-build-dir/fake-final-name"));
    Assert.assertEquals(expected, testMojo.getServices());
  }

  @Test
  public void testGetServices_empty() throws NoSuchFieldException, IllegalAccessException {
    testMojo.injectServices(ImmutableList.of());
    List<Path> expected = ImmutableList.of(Paths.get("fake-build-dir/fake-final-name"));
    Assert.assertEquals(expected, testMojo.getServices());
  }

  @Test
  public void testGetServices_validList() throws NoSuchFieldException, IllegalAccessException {
    testMojo.injectServices(ImmutableList.of(new File("some-service-location")));
    Assert.assertEquals(
        ImmutableList.of(Paths.get("some-service-location")), testMojo.getServices());
  }
}
