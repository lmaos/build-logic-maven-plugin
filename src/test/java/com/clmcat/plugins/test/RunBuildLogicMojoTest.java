package com.clmcat.plugins.test;

import com.clmcat.maven.plugins.HelloMojo;
import com.clmcat.maven.plugins.RunBuildLogicMojo;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.inject.Inject;

@MojoTest
public class RunBuildLogicMojoTest  {


    @Inject
            @Mock
    MavenProject project;

    @Test
    @Basedir("src/test/resources/plugin-test-hello")
    @InjectMojo(goal = "hello", pom = "plugin-test-hello-pom.xml")
    public void testHelloMojoExecute(HelloMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }

    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-list-pom.xml")
    public void testListMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }


    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-return-pom.xml")
    public void testReturnMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }

    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-file-pom.xml")
    public void testFileMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }

    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-http-pom.xml")
    public void testHttpMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }
    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-str-pom.xml")
    public void testStringMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }

    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-base64-pom.xml")
    public void testBase64MojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }

    @Test
    @Basedir("src/test/resources/plugin-test-run")
    @InjectMojo(goal = "run", pom = "plugin-test-date-pom.xml")
    public void testDateMojoExecute(RunBuildLogicMojo mojo)  {
        Assertions.assertDoesNotThrow(() -> mojo.execute());
    }
}
