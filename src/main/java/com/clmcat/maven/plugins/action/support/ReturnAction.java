package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.*;
import com.clmcat.maven.plugins.action.variable.VariableFactory;

public class ReturnAction extends Action.AbstractAction {

    // return reference
    private String ref;

    private String type;

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        if (XUtils.isEmpty(ref)) {
            String value = getValue();
            Variable variable = VariableFactory.newVariable(type, value);
            throw new FunctionAction.ReturnException(variable);
        } else {
            Variable variable = getFunctionVariables().getVariable(ref);
            throw new FunctionAction.ReturnException(variable);
        }
    }
}
