package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class FileAction extends VariableAction {

    private String path;

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

        if (this.path == null) {
            throw new MojoExecutionException("<file file=null> file is null");
        }

        if (getName() == null) {
            throw new MojoExecutionException("<file name=null> name is null");
        }

        String fileName = actionParam.format(this.path);

        File file = new File(fileName);
        setVariable(actionParam, new FileVariable(file));
        actionParam.info("set file variable: " + getName() + " -> " + file.getAbsolutePath());

    }
}
