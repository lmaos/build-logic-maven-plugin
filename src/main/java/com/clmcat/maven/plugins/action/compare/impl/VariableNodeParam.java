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
        /// 两个变量均不存在时,判断是否为数字,如果是数字,则转换为数字变量,否则转换为字符串变量
        if (leftVariable == null && rightVariable == null) {
            if (isNumber(left) && isNumber(right)) {
                leftVariable =  NumberVariable.of(left);
                rightVariable = NumberVariable.of(right);
            } else {
                leftVariable = StringVariable.of(unquote(left));
                rightVariable = StringVariable.of(unquote(right));
            }
        } else if (leftVariable == null && rightVariable != null) {
            // 左值不存在，右值存在时， 通过右边的值解析left变量，
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
            // 右值不存在，通过左边的值解析right变量，
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
