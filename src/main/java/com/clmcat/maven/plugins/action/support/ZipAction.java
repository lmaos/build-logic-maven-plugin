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
 * 压缩文件.
 * 使用方法:
 * <pre>
 * {@code
 *  <!-- 创建文件对象 -->
 *  <file name="appZipFile" file="${project.basedir}/app.zip"/>
 *  <!-- 压缩文件 -->
 *  <zip file="appZipFile">
 *      <!-- 压缩目录 -->
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
            byte[] buffer = new byte[8192]; // 缓冲区

            for (ZipEntryAction.ZipEntry zipEntry : filesToZip) {
                // 创建 ZIP 条目（保持文件名，如需带目录可自行修改）
                zos.putNextEntry(zipEntry.getZipEntry());
                // 读取文件并写入 ZIP
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

                // 解析目录
                this.dir = actionParam.format(this.dir);

                actionParam.info("zip.entry, dir: " + dir+", pattern: " + pattern);
                // 必须在 zip 内执行
                if (!(parentAction instanceof ZipAction)) {
                    throw new MojoExecutionException("This operation must be executed within <zip></zip>");
                }

                File rootDir = new File(getDir());
                // 校验目录
                if (!rootDir.exists()) {
                    throw new MojoExecutionException("Directory not exists: " + rootDir.getAbsolutePath());
                }
                if (!rootDir.isDirectory()) {
                    throw new MojoExecutionException("Path is not a directory: " + rootDir.getAbsolutePath());
                }

                // 存储所有匹配到的文件
                List<File> matchedFiles = new ArrayList<>();

                // 递归遍历目录 + 子目录
                listFiles(rootDir, getPattern(), matchedFiles);

                // 日志输出
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
        // 递归遍历：目录 + 子目录 + 模糊匹配
        // =========================================
        private void listFiles(File currentDir, String pattern, List<File> matchedFiles) {
            File[] files = currentDir.listFiles();
            if (files == null) return;

            for (File file : files) {
                // 是目录 → 递归进去
                if (file.isDirectory()) {
                    listFiles(file, pattern, matchedFiles);
                }
                // 是文件 → 匹配
                else if (wildcardMatch(file.getName(), pattern)) {
                    matchedFiles.add(file);
                }
            }
        }


        private boolean wildcardMatch(String fileName, String pattern) {
            if (pattern == null || pattern.isEmpty()) return true;
            if (pattern.equals("*")) return true;
            // =========================================
            // 规则 1：以 regex: 开头 → 直接按标准正则执行
            // =========================================
            if (pattern.startsWith("regex:")) {
                String realRegex = pattern.substring("regex:".length());
                return fileName.matches(realRegex);
            }

            // =========================================
            // 规则 2：默认 → 简单通配符（仅 * 代表任意字符）
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
