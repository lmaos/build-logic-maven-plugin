package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.variable.FileVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ActionFileSupport {

    private ActionFileSupport() {
    }

    public static File getAllowedWriteDir(ActionParam actionParam) {
        Variable<File> variable = actionParam.getVariable("allowWriteDir");
        if (!Variable.isExist(variable)) {
            return null;
        }
        return variable.getValue();
    }

    public static File resolveFile(Action.AbstractAction action, ActionParam actionParam, String referenceOrPath,
                                   String attributeName, boolean allowPathFallback) throws MojoExecutionException {
        String formatted = actionParam.format(referenceOrPath);
        if (XUtils.isEmpty(formatted)) {
            throw new MojoExecutionException("Action <" + action.getTag() + "> requires non-empty " + attributeName + " attribute");
        }

        Variable variable = action.getVariable(formatted);
        if (variable instanceof FileVariable) {
            File file = ((FileVariable) variable).getValue();
            if (file == null) {
                throw new MojoExecutionException("Action <" + action.getTag() + "> file variable is null: " + formatted);
            }
            return file;
        }

        if (allowPathFallback) {
            return new File(formatted);
        }

        throw new MojoExecutionException("Action <" + action.getTag() + "> requires " + attributeName
                + " to reference a <file> variable: " + formatted);
    }

    public static void requireSafeWriteTarget(Action.AbstractAction action, ActionParam actionParam, File file)
            throws MojoExecutionException {
        try {
            if (!isSafeWriteTarget(actionParam, file)) {
                throw new MojoExecutionException("Unsafe file target for <" + action.getTag() + ">: " + file
                        + ", allowWriteDir=" + getAllowedWriteDir(actionParam));
            }
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to validate safe file target: " + file, exception);
        }
    }

    public static boolean isSafeWriteTarget(ActionParam actionParam, File file) throws IOException {
        if (file == null) {
            return false;
        }

        String canonicalPath = file.getCanonicalPath().toLowerCase();
        if (canonicalPath.equals("/")
                || canonicalPath.matches("^[a-z]:\\\\?$")
                || canonicalPath.contains("windows")
                || canonicalPath.contains("system32")
                || canonicalPath.contains("/bin")
                || canonicalPath.contains("/sbin")
                || canonicalPath.contains("/usr")
                || canonicalPath.contains("/etc")) {
            return false;
        }

        File allowDir = getAllowedWriteDir(actionParam);
        if (allowDir == null) {
            return false;
        }

        String allowCanonicalPath = allowDir.getCanonicalPath().toLowerCase();
        return canonicalPath.startsWith(allowCanonicalPath);
    }

    public static void ensureFileExists(File file, String description) throws MojoExecutionException {
        if (file == null || !file.exists()) {
            throw new MojoExecutionException(description + " does not exist: " + file);
        }
    }

    public static void ensureRegularFile(File file, String description) throws MojoExecutionException {
        ensureFileExists(file, description);
        if (!file.isFile()) {
            throw new MojoExecutionException(description + " is not a file: " + file);
        }
    }

    public static void ensureDirectory(File file, String description) throws MojoExecutionException {
        ensureFileExists(file, description);
        if (!file.isDirectory()) {
            throw new MojoExecutionException(description + " is not a directory: " + file);
        }
    }

    public static void ensureParentDirectoryExists(File file, String description) throws MojoExecutionException {
        File parent = file == null ? null : file.getParentFile();
        if (parent == null || !parent.exists() || !parent.isDirectory()) {
            throw new MojoExecutionException(description + " parent directory does not exist: " + parent);
        }
    }

    public static void deleteRecursively(File file) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(file.toPath())) {
            stream.sorted(Comparator.reverseOrder()).forEach(paths::add);
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
