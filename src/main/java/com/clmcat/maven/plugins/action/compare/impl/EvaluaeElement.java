package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;

public class EvaluaeElement implements EvaluateNode {

    private String left;
    private String right;
    private String symbol;
    public EvaluaeElement(String left, String right, String symbol) {
        this.left = left;
        this.right = right;
        this.symbol = symbol;
    }


    public boolean evaluate(NodeParam nodeParam) {

        NodeParam.NodeVariable nodeVariable = nodeParam.getNodeVariable(left, right);

        Variable leftVariable = nodeVariable.getLeftVariable();
        Variable rightVariable = nodeVariable.getRightVariable();

        int v = leftVariable.compareTo(rightVariable);
        if ("==".equals(symbol)) {
            return v == 0;
        }
        if ("!==".equals(symbol)) {
            return v != 0;
        }

        if (">=".equals(symbol)) {
            return v >= 0;
        }

        if ("<=".equals(symbol)) {
            return v <= 0;
        }

        if (">".equals(symbol)) {
            return v > 0;
        }
        if ("<".equals(symbol)) {
            return v < 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return left + " " + symbol + " " + right;
    }
}
