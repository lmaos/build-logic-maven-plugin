package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Variables held within a function scope.
 */
public class FunctionVariableItem implements FunctionVariable {

    private Action action;

    public FunctionVariableItem(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    /**
     * Variable map for the current function scope.
     */
    private Map<String, Variable> variables = new HashMap<String, Variable>();

    public void setVariable(String name, Variable value) {
        if (value != null) {
            variables.put(name, value);
//            if (value instanceof Variable) {
//                variables.put(name, (Variable) value);
//            } else if (value instanceof List) {
//                variables.put(name, new ListVariable((List) value));
//            } else if (value instanceof File) {
//                variables.put(name, new FileVariable((File) value));
//            } else if (value instanceof FunctionAction) {
//                variables.put(name, new FunctionActionVariables((FunctionAction) value));
//            } else if (value instanceof String) {
//                variables.put(name, new StringVariable((String) value));
//            } else {
//                variables.put(name, new DefaultVariable(value));
//            }
        } else {
            variables.remove(name);
        }
    }

    public Variable getVariable(String name) {
        return getVariable(name, Variable.NULL);
    }

    public Variable getVariable(String name, Variable defaultVariable) {
        Variable variable = variables.get(name);
        if (variable != null) {
            return variable;
        } else {
            return defaultVariable;
        }
    }

    public void removeVariable(String name) {
        variables.remove(name);
    }

    public Set<String> getVariableNames() {
        return variables.keySet();
    }

    public void clearVariables() {
        variables.clear();
    }

}
