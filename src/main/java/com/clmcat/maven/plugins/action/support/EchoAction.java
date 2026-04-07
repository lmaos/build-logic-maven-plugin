package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;

public class EchoAction extends Action.AbstractAction {
    private String level;
    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String value = getValue();
        if ("info".equalsIgnoreCase(level)) {
            actionParam.info(value);
        }  else if ("error".equalsIgnoreCase(level)) {
            actionParam.error(value);
        }  else if ("warn".equalsIgnoreCase(level)) {
            actionParam.warn(value);
        }   else if ("debug".equalsIgnoreCase(level)) {
            actionParam.debug(value);
        }  else {
            actionParam.info(value);
        }
    }
}
