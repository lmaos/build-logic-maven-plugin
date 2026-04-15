package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionFileSupport;
import com.clmcat.maven.plugins.action.ActionParam;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.nio.file.Files;

public class DeleteAction extends Action.AbstractAction {

    private String file;

    private boolean force;

    @Override
    protected void callExecute(ActionParam actionParam, Action action) throws Exception {
        File targetFile = ActionFileSupport.resolveFile(this, actionParam, this.file, "file", true);
        if (!force) {
            ActionFileSupport.requireSafeWriteTarget(this, actionParam, targetFile);
        }

        if (!targetFile.exists()) {
            actionParam.warn("删除的文件已经不存在: " + targetFile);
            return;
        }
        if (targetFile.isDirectory()) {
            actionParam.info("正在进行删除目录: " + targetFile);
            ActionFileSupport.deleteRecursively(targetFile);
        } else if (!Files.deleteIfExists(targetFile.toPath())) {
            throw new MojoExecutionException("删除文件失败: " + targetFile);
        }
        actionParam.info("delete file:" + targetFile + ", success");
    }
}
