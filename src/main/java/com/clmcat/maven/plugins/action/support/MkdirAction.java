package com.clmcat.maven.plugins.action.support;


import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionFileSupport;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.XUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class MkdirAction extends Action.AbstractAction {

    private String path;

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

        String path = this.path;
        if (XUtils.isEmpty(path)) {
            path = getValue();
        }
        path = actionParam.format(path);
        if (XUtils.isEmpty(path)) {
            throw new MojoExecutionException("<mkdir> requires path attribute or body text");
        }

        File file = new File(path);
        ActionFileSupport.requireSafeWriteTarget(this, actionParam, file);
        if (file.exists() && !file.isDirectory()) {
            throw new MojoExecutionException("mkdir target exists but is not a directory: " + file);
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new MojoExecutionException("Failed to create directory: " + file);
        }
        actionParam.info("success, mkdir path:" + path);
    }
}
