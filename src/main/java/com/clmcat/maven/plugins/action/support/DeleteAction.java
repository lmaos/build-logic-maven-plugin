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
                    actionParam.info("Deleting directory: " + file );
                    Files.walk(file.toPath())
                            .sorted(java.util.Comparator.reverseOrder()) // children first, then parents
                            .forEach(p -> {
                                try {
                                    actionParam.info("Deleting: " + p);
                                    Files.delete(p);
                                } catch (Exception e) {
                                    // ignore
                                    actionParam.error("Failed to delete: " + p + ", exception: ", e);
                                }
                            });
                }
                file.delete();
            } else{
                actionParam.warn("File to delete does not exist: " + file);
            }
        } else {
            actionParam.warn("Cannot delete: path is outside the safe directory. Use force='true' to override. Safe directory: " + getSafeDir(actionParam) + ", target file: " + file);
        }


        actionParam.info("delete file:" + file + ", success");
    }
}
