package com.clmcat.maven.plugins.action.support;


import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.XUtils;

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

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            actionParam.info("success, mkdir path:" + path);
        }

    }
}
