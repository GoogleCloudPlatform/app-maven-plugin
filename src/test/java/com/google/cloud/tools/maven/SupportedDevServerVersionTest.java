package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;

import com.google.cloud.tools.maven.AppEngineFactory.SupportedDevServerVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SupportedDevServerVersionTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
