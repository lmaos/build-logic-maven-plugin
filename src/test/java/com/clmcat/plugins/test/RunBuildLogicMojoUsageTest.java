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
class RunBuildLogicMojoUsageTest {

    private static final Path USAGE_BASEDIR = Paths.get("src", "test", "resources", "plugin-test-usage");
    private static final Path USAGE_OUTPUT = USAGE_BASEDIR.resolve("usage-output.txt");

    @Test
    @Basedir("src/test/resources/plugin-test-usage")
    @InjectMojo(goal = "run", pom = "plugin-test-usage-pom.xml")
    void shouldExecuteDocumentedDslFlow(RunBuildLogicMojo mojo) throws Exception {
        Files.deleteIfExists(USAGE_OUTPUT);

        Assertions.assertDoesNotThrow(mojo::execute);
        Assertions.assertEquals("HELLO DSL|2|hello dsl|done|OK", readUtf8(USAGE_OUTPUT));
    }

    private String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
