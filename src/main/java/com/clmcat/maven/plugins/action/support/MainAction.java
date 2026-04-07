package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.FunctionAction;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * 主函数。入口函数。
 */
public class MainAction extends FunctionAction.AbstractFunctionAction {

    public MainAction() {
       super(true);
       setName("main");
    }

    @Override
    protected boolean initFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception {
        if (parentAction != null) {
            throw new MojoExecutionException("main action must be the first action in");
        }
        return true;
    }

}
