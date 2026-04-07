package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;

public class DefaultNodeVariable implements NodeParam.NodeVariable {
    Variable leftVariable;
    Variable rightVariable;

    public DefaultNodeVariable(Variable leftVariable, Variable rightVariable) {
        this.leftVariable = leftVariable;
        this.rightVariable = rightVariable;
    }

    @Override
    public Variable getLeftVariable() {
        return leftVariable;
    }

    @Override
    public Variable getRightVariable() {
        return rightVariable;
    }
}
