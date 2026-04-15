package com.clmcat.plugins.test;

import com.clmcat.maven.plugins.RunBuildLogicMojo;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@MojoTest
class RunBuildLogicMojoRegressionTest {

    private static final Path REGRESSION_BASEDIR = Paths.get("src", "test", "resources", "plugin-test-regression");
    private static final Path FORMAT_OUTPUT = REGRESSION_BASEDIR.resolve("regression-format-output.txt");
    private static final Path HTTP_OUTPUT = REGRESSION_BASEDIR.resolve("regression-http-output.txt");

    @Test
    @Basedir("src/test/resources/plugin-test-regression")
    @InjectMojo(goal = "run", pom = "regression-format-pom.xml")
    void shouldSupportQuotedArgumentsAndAssignableMethodResolution(RunBuildLogicMojo mojo) throws Exception {
        Files.deleteIfExists(FORMAT_OUTPUT);

        Assertions.assertDoesNotThrow(mojo::execute);
        Assertions.assertEquals("1|2|3", readUtf8(FORMAT_OUTPUT));
    }

    @Test
    @Basedir("src/test/resources/plugin-test-regression")
    @InjectMojo(goal = "run", pom = "regression-http-pom.xml")
    void shouldSupportUnnamedHttpWithStructuredResponseVariables(RunBuildLogicMojo mojo) throws Exception {
        Files.deleteIfExists(HTTP_OUTPUT);

        try (LocalHttpTestServer ignored = LocalHttpTestServer.start()) {
            Assertions.assertDoesNotThrow(mojo::execute);
        }
        Assertions.assertEquals("200|application/json;charset=UTF-8|{\"message\":\"headers\"}", readUtf8(HTTP_OUTPUT));
    }

    private String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
