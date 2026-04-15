package com.clmcat.plugins.test;

import com.clmcat.maven.plugins.RunBuildLogicMojo;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MojoTest
class RunBuildLogicMojoBoundaryTest {

    @Test
    @Basedir("src/test/resources/plugin-test-boundary")
    @InjectMojo(goal = "run", pom = "boundary-missing-value-pom.xml")
    void shouldFailFastWhenVariableHasNoValueOrReference(RunBuildLogicMojo mojo) {
        MojoExecutionException exception = Assertions.assertThrows(MojoExecutionException.class, mojo::execute);
        Assertions.assertTrue(exception.getMessage().contains("Variable action requires value or ref"));
    }

    @Test
    @Basedir("src/test/resources/plugin-test-boundary")
    @InjectMojo(goal = "run", pom = "boundary-foreach-missing-pom.xml")
    void shouldFailWhenForeachCollectionIsUnknown(RunBuildLogicMojo mojo) {
        MojoExecutionException exception = Assertions.assertThrows(MojoExecutionException.class, mojo::execute);
        Assertions.assertTrue(exception.getMessage().contains("Unknown foreach collection"));
    }

    @Test
    @Basedir("src/test/resources/plugin-test-boundary")
    @InjectMojo(goal = "run", pom = "boundary-write-outside-pom.xml")
    void shouldRejectUnsafeWriteTargets(RunBuildLogicMojo mojo) {
        MojoExecutionException exception = Assertions.assertThrows(MojoExecutionException.class, mojo::execute);
        Assertions.assertTrue(exception.getMessage().contains("Unsafe file target"));
    }
}
