package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.FunctionAction;
import com.clmcat.maven.plugins.action.Variable;

public class FunctionActionVariable extends Variable.AbstractVariable<FunctionAction>{
    public FunctionActionVariable(FunctionAction value) {
        super(value);
    }

    public void execute(ActionParam actionParam, Action parentAction, Variable...args) throws Exception {
        getValue().triggerFunctionExecute(actionParam, parentAction);
    }


    public static FunctionActionVariable of(FunctionAction value) {
        return new FunctionActionVariable(value);
    }



}
