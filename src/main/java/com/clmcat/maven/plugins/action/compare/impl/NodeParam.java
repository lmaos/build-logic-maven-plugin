package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;

public interface NodeParam {


    /**
     * Read node parameter.
     * @param left left-side variable name
     * @param right right-side variable name
     * @return the node variable instance
     */
    public NodeVariable getNodeVariable(String left, String right);

    /**
     * Return the variable by name, or null if it does not exist.
     * @param name variable name
     * @return variable instance
     */
    Variable getVariable(String name);

    /**
     * Read the variable value.
     */
    interface NodeVariable {
        Variable getLeftVariable();
        Variable getRightVariable();
    }
}
