package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.FunctionAction;
import com.clmcat.maven.plugins.action.Variable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 函数内变量
 */
public interface FunctionVariable {

    default Action getAction() {
        return null;
    }
    public void setVariable(String name, Variable value);

    public Variable getVariable(String name);

    public Variable getVariable(String name, Variable defaultVariable);

    public void removeVariable(String name);

    public Set<String> getVariableNames() ;

    public void clearVariables();

}
