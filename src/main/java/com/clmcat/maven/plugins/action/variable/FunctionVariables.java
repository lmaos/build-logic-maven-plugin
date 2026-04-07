package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class FunctionVariables extends LinkedList<FunctionVariable> implements FunctionVariable {

    public Variable getVariable(String name) {
        return getVariable(name, Variable.NULL);
    }

    public Variable getVariable(String name, Variable defaultVariable) {
        for (FunctionVariable functionVariables : this) {
            Variable variable = functionVariables.getVariable(name, null);
            if (variable != null) {
                return variable;
            }
        }
        return defaultVariable;
    }

    @Override
    public void removeVariable(String name) {
        for (FunctionVariable functionVariables : this) {
            Variable variable = functionVariables.getVariable(name, null);
            if (variable != null) {
                functionVariables.removeVariable(name);
                break;
            }
        }
    }

    @Override
    public Set<String> getVariableNames() {
        return Collections.emptySet();
    }

    @Override
    public void clearVariables() {
        for (FunctionVariable functionVariables : this) {
            functionVariables.clearVariables();
        }
    }


    public void setVariable(String name, Variable value) {
        boolean found = false;
        for (FunctionVariable functionVariables : this) {
            Variable variable = functionVariables.getVariable(name, null);
            if (variable != null) {
                functionVariables.setVariable(name, value);
                found = true;
                break;
            }
        }
        if (!found && !isEmpty()) {
            getFirst().setVariable(name, value);
        }
    }

    public void setRootVariable(String name, Variable value) {
       getLast().setVariable(name, value);
    }

    private Set<FunctionVariable> functionVariables = new HashSet<>();
    public boolean contains(FunctionVariable o) {
        return functionVariables.contains(o);
    }

    @Override
    public void addFirst(FunctionVariable functionVariable) {
        if (functionVariables.add(functionVariable)) {
            super.addFirst(functionVariable);
        }
    }

    @Override
    public void addLast(FunctionVariable functionVariable) {

        if (functionVariables.add(functionVariable)) {
            super.addLast(functionVariable);
        }

    }

    @Override
    public boolean add(FunctionVariable functionVariable) {
        if (functionVariables.add(functionVariable)) {
            return super.add(functionVariable);
        }
        return false;
    }

    @Override
    public void add(int index, FunctionVariable element) {
        if (functionVariables.add(element)) {
            super.add(index, element);
        }
    }

    @Override
    public FunctionVariable remove() {
        FunctionVariable remove = super.remove();
        if (remove != null) {
            functionVariables.remove(remove);
        }
        return remove;
    }

    @Override
    public boolean remove(Object o) {

        super.remove(o);
        functionVariables.remove(o);

        return true;
    }

    @Override
    public FunctionVariable removeFirst() {
        FunctionVariable remove =  super.removeFirst();
        if  (remove != null) {
            functionVariables.remove(remove);
        }
        return remove;
    }

    @Override
    public FunctionVariable removeLast() {
        FunctionVariable remove =  super.removeLast();
        if (remove != null) {
            functionVariables.remove(remove);
        }
        return remove;
    }

    @Override
    public FunctionVariable remove(int index) {
        FunctionVariable remove = super.remove(index);
        if (remove != null) {
            functionVariables.remove(remove);
        }
        return remove;
    }

    @Override
    public void clear() {
        clearVariables();
        super.clear();
    }
}
