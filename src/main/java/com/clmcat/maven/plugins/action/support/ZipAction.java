package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.*;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import com.clmcat.maven.plugins.action.variable.ListVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;


/**
 * Zip file compression.
 * Usage:
 * <pre>
 * {@code
 *  <!-- Create a file object -->
 *  <file name="appZipFile" file="${project.basedir}/app.zip"/>
 *  <!-- Compress files -->
 *  <zip file="appZipFile">
 *      <!-- Compress a directory -->
 *     <entry dir="${project.basedir}/test_dir"/>
 * </zip>
 *}</pre>
 * @author zxy
 *
 *
 */
public class ZipAction extends CodeBlockAction.AbstractCodeBlockAction {

    private String file;

    @Override
    protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
        ActionFactory actionFactory = actionParam.getActionExecute().getActionFactory().create();
        actionFactory.addActionType("entry", ZipEntryAction.class);
        actions = parseChildren(actionFactory);
        super.callCodeBlockExecute(actionParam, parentAction, actions);
    }

    @Override
    protected void afterExecute(ActionParam actionParam, Action parentAction) throws Exception {

        this.file = actionParam.format(file);

        actionParam.info("<zip> zip.file: " + file);

        File zipFile = null;
        Variable variable = getFunctionVariables().getVariable(this.file);
        if (variable instanceof FileVariable) {
            zipFile = ((FileVariable) variable).getValue();
        } else {
            zipFile =  new File(file);
        }
        if (zipFile.getParentFile() == null || !zipFile.getParentFile().exists()) {
            throw new MojoExecutionException("Directory does not exist, file: " + this.file);
        }

        Variable<List<ZipEntryAction.ZipEntry>> fileVariable = getFunctionVariables().getVariable(ZipEntryAction.VAR_NAME);
        if (fileVariable == null || !fileVariable.isExist()) {
            actionParam.info("No files to zip");
            return;
        }
        List<ZipEntryAction.ZipEntry> filesToZip = fileVariable.getValue();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[8192]; // buffer

            for (ZipEntryAction.ZipEntry zipEntry : filesToZip) {
                // Create a ZIP entry (file name is kept as-is; adjust if directory prefix is needed)
                zos.putNextEntry(zipEntry.getZipEntry());
                // Read the file and write it to the ZIP
                try (FileInputStream fis = new FileInputStream(zipEntry.getFile())) {
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
                actionParam.info("Zipped: " + zipEntry.getZipEntry().getName());
            }

            actionParam.info("Zip completed successfully!");
        }
    }


    public static class ZipEntryAction extends AbstractAction {

        public final static String VAR_NAME = "zipEntrys";


        private String dir;
        private String pattern;

        private List<ZipEntry> matchedFiles = new ArrayList<>();

        public String getDir() {
            return dir;
        }

        public String getPattern() {
            return pattern;
        }

        public List<ZipEntry> getMatchedFiles() {
            return matchedFiles;
        }

        @Override
        protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

                // Resolve the directory
                this.dir = actionParam.format(this.dir);

                actionParam.info("zip.entry, dir: " + dir+", pattern: " + pattern);
                // Must be executed inside <zip></zip>
                if (!(parentAction instanceof ZipAction)) {
                    throw new MojoExecutionException("This operation must be executed within <zip></zip>");
                }

                File rootDir = new File(getDir());
                // Validate the directory
                if (!rootDir.exists()) {
                    throw new MojoExecutionException("Directory not exists: " + rootDir.getAbsolutePath());
                }
                if (!rootDir.isDirectory()) {
                    throw new MojoExecutionException("Path is not a directory: " + rootDir.getAbsolutePath());
                }

                // Collect all matching files
                List<File> matchedFiles = new ArrayList<>();

                // Recursively traverse directory and subdirectories
                listFiles(rootDir, getPattern(), matchedFiles);

                // Log output
                actionParam.info("Found matched files: " + matchedFiles.size());
                for (File file : matchedFiles) {
                    if (file.isFile() && file.exists()) {
                        String relative = file.getAbsolutePath()
                                .substring(rootDir.getAbsolutePath().length())
                                .replace(File.separatorChar, '/')
                                .replaceFirst("^/", "");
                        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(relative);
                        this.matchedFiles.add(new ZipEntry(zipEntry, file, rootDir));
                        actionParam.info(" -> " + file.getAbsolutePath());
                    }
                }


            Variable<List<ZipEntry>> variable = getFunctionVariables().getVariable(VAR_NAME);

            if (!variable.isExist()) {
                variable = new ListVariable<>();
                getFunctionVariables().setVariable(VAR_NAME, variable);
            }

            ((ListVariable<ZipEntry>)variable).addAll(this.matchedFiles);
        }

        // =========================================
        // Recursive traversal: directory + subdirectories + wildcard matching
        // =========================================
        private void listFiles(File currentDir, String pattern, List<File> matchedFiles) {
            File[] files = currentDir.listFiles();
            if (files == null) return;

            for (File file : files) {
                // Is a directory → recurse into it
                if (file.isDirectory()) {
                    listFiles(file, pattern, matchedFiles);
                }
                // Is a file → check pattern match
                else if (wildcardMatch(file.getName(), pattern)) {
                    matchedFiles.add(file);
                }
            }
        }


        private boolean wildcardMatch(String fileName, String pattern) {
            if (pattern == null || pattern.isEmpty()) return true;
            if (pattern.equals("*")) return true;
            // =========================================
            // Rule 1: starts with "regex:" → use it as a standard Java regex
            // =========================================
            if (pattern.startsWith("regex:")) {
                String realRegex = pattern.substring("regex:".length());
                return fileName.matches(realRegex);
            }

            // =========================================
            // Rule 2: default → simple wildcard (only * represents any characters)
            // =========================================
            String regex = pattern.replace(".","\\.").replace("*",".*");
            return fileName.matches(regex);
        }


        public static class ZipEntry {
            java.util.zip.ZipEntry zipEntry;
            private File file;
            private File directory;

            public ZipEntry(java.util.zip.ZipEntry zipEntry, File file, File directory) {
                this.zipEntry = zipEntry;
                this.file = file;
                this.directory = directory;
            }

            public java.util.zip.ZipEntry getZipEntry() {
                return zipEntry;
            }

            public File getFile() {
                return file;
            }

            public File getDirectory() {
                return directory;
            }
        }
    }
}
