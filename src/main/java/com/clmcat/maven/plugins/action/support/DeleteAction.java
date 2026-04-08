package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.FileVariable;

import java.io.File;
import java.nio.file.Files;

public class DeleteAction extends Action.AbstractAction {

    private String file;

    private boolean force;

    @Override
    protected void callExecute(ActionParam actionParam, Action action) throws Exception {
        this.file = actionParam.format(this.file);
        final Variable variable = getVariable(file);
        final File file;
        if (variable instanceof FileVariable) {
            file = ((FileVariable)variable).getValue();
        } else {
            file = new File(this.file);
        }


        if (force || safeDir(actionParam, file)) {

            if (file.exists()) {
                if (file.isDirectory()) {
                    actionParam.info("正在进行删除目录: " + file );
                    Files.walk(file.toPath())
                            .sorted(java.util.Comparator.reverseOrder()) // 先子后父
                            .forEach(p -> {
                                try {
                                    actionParam.info("删除文件: " + p);
                                    Files.delete(p);
                                } catch (Exception e) {
                                    // ignore
                                    actionParam.error("删除文件失败: " + p + ", 异常: ", e);
                                }
                            });
                }
                file.delete();
            } else{
                actionParam.warn("删除的文件已经不存在: " + file +"");
            }
        } else {
            actionParam.warn("无法成功删除， 不再安全目录下， 强制删除设置属性： force='true' , 当前安全目录: " + getSafeDir(actionParam) + ", 删除文件: " + file);
        }


        actionParam.info("delete file:" + file + ", success");
    }
}
