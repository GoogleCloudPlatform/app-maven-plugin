package com.google.cloud.tools.maven;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.debug.GenRepoInfoFile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Paths;

/**
 * {@link GenRepoInfoFileMojo} unit tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class GenRepoInfoFileMojoTest {
  @Mock
  private CloudSdkAppEngineFactory factory;

  @Mock
  private GenRepoInfoFile genMock;

  @InjectMocks
  private GenRepoInfoFileMojo genMojo;

  @Before
  public void init() {
    genMojo.sourceDirectory = Paths.get("/a/b/c/source").toFile();
    genMojo.outputDirectory = "/e/f/g/output";
    when(factory.genRepoInfoFile()).thenReturn(genMock);
  }

  @Test
  public void testExecute() throws MojoFailureException, MojoExecutionException {
    genMojo.ignoreErrors = true;
    genMojo.execute();

    verify(genMock).generate(genMojo);
  }

  @Test(expected = MojoExecutionException.class)
  public void testExecute_noIgnoreErrors() throws MojoFailureException, MojoExecutionException {
    doThrow(new AppEngineException("Bad")).when(genMock).generate(genMojo);

    genMojo.execute();

    verify(genMock).generate(genMojo);
  }
}
