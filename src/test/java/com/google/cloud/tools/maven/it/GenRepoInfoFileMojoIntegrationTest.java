package com.google.cloud.tools.maven.it;

import com.google.cloud.tools.maven.it.verifier.StandardVerifier;
import java.io.IOException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

/** {@link com.google.cloud.tools.maven.GenRepoInfoFileMojo} integration tests. */
public class GenRepoInfoFileMojoIntegrationTest extends AbstractMojoIntegrationTest {
  @Test
  public void testGenerate() throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier("testGenRepoInfoFile");

    verifier.executeGoal("appengine:genRepoInfoFile");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF/classes/source-context.json");
  }

  /** Ensures that this goal is ran with the package phase. */
  @Test
  public void testGenerateCallingPackage() throws IOException, VerificationException {
    Verifier verifier = new StandardVerifier("testGenRepoInfoFile");

    verifier.executeGoal("package");
    verifier.assertFilePresent("target/appengine-staging/WEB-INF/classes/source-context.json");
  }
}
