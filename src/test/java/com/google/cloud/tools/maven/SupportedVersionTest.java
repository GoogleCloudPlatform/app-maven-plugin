package com.google.cloud.tools.maven;

import static org.junit.Assert.assertEquals;

import com.google.cloud.tools.maven.AppEngineFactory.SupportedVersion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SupportedVersionTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testParseV1() {
    assertEquals(SupportedVersion.V1, SupportedVersion.parse("1"));
  }

  @Test
  public void testParseV2Alpha() {
    assertEquals(SupportedVersion.V2ALPHA, SupportedVersion.parse("2-alpha"));
  }

  @Test
  public void testParseInvalidVersion() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unsupported version value: foo");

    SupportedVersion.parse("foo");
  }
}
