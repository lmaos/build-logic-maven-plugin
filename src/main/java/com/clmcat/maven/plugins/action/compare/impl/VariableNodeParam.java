package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.BytesVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;

public class VariableNodeParam implements NodeParam {
    private FunctionVariable variable;


    public VariableNodeParam(FunctionVariable variable) {
        this.variable = variable;
    }

    @Override
    public NodeVariable getNodeVariable(String left, String right) {

        Variable leftVariable = variable.getVariable(left);
        Variable rightVariable = variable.getVariable(right);
        /// When neither variable exists, check if both sides are numbers; if so create NumberVariables, otherwise StringVariables
        if (leftVariable == null && rightVariable == null) {
            if (isNumber(left) && isNumber(right)) {
                leftVariable =  NumberVariable.of(left);
                rightVariable = NumberVariable.of(right);
            } else {
                leftVariable = StringVariable.of(unquote(left));
                rightVariable = StringVariable.of(unquote(right));
            }
        } else if (leftVariable == null && rightVariable != null) {
            // Left value does not exist but right does: resolve left based on the right type
            if (rightVariable instanceof StringVariable) {
                leftVariable = StringVariable.of(unquote(left));
            }  else if (rightVariable instanceof NumberVariable) {
                if (!isNumber(left)) {
                    throw new IllegalArgumentException("left value must be number, left : " + left);
                }
                leftVariable = NumberVariable.of(unquote(left));
            } else if (rightVariable instanceof BytesVariable) {
                leftVariable = BytesVariable.of(left);
            } else {
                throw new IllegalArgumentException("not support type : "  + rightVariable.getClass().getSimpleName() + ", left : " + left +", right : " + right);
            }
        } else  {
            // Right value does not exist: resolve right based on the left type
            if (leftVariable instanceof StringVariable) {
                rightVariable = StringVariable.of(unquote(right));
            }  else if (leftVariable instanceof NumberVariable) {
                if (!isNumber(right)) {
                    throw new IllegalArgumentException("left value must be number, left : " + left);
                }
                rightVariable = NumberVariable.of(unquote(right));
            } else if (leftVariable instanceof BytesVariable) {
                rightVariable = BytesVariable.of(right);
            } else {
                throw new IllegalArgumentException("not support type : "  + leftVariable.getClass().getSimpleName()  + ", left : " + left +", right : " + right);
            }
        }

        return new DefaultNodeVariable(leftVariable, rightVariable);
    }

    @Override
    public Variable getVariable(String name) {
        return variable.getVariable(name, null);
    }

    private boolean isNumber(String text) {
        return XUtils.isNumber(text);
    }

    private String unquote(String str) {
        return  XUtils.unquote(str);
    }
}
