package com.clmcat.maven.plugins.action.compare;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;

public class DefaultCompare implements Compare{
    @Override
    public boolean compare(String test, FunctionVariable functionVariable) {
        if (test == null || (test = test.trim()).isEmpty()) {
            return true;
        }


        int i = test.indexOf("==");
        int j = test.indexOf("!=");

        if (i == -1 && j== -1){
            boolean flag = test.charAt(0) == '!';
            Variable variable;
            if (flag) {
                variable = functionVariable.getVariable(test.substring(1, test.length()));
            }  else {
                variable = functionVariable.getVariable(test);
            }

            boolean result =  false;

            if (variable.isExist()) { // 如果变量存在

                if (variable.getValue() instanceof Boolean) {
                    result = (Boolean) variable.getValue();
                } else if (variable instanceof FileVariable) {
                    result = true;
                } else if ("true".equalsIgnoreCase(variable.getStringValue())) {
                    result = true;
                } else if ("false".equalsIgnoreCase(variable.getStringValue())) {
                    result = false;
                }  else {
                    result = true;
                }
            }
            return flag ? !result : result;
        } else if (i != -1) {

            String left = test.substring(0, i).trim();
            String right = test.substring(i+2).trim();

            Variable variable1 = functionVariable.getVariable(left, StringVariable.of(left));
            Variable variable2 = functionVariable.getVariable(right, StringVariable.of(right));
            return variable1.getStringValue().equals(variable2.getStringValue());

        } else if (j != -1) {

            String left = test.substring(0, j).trim();
            String right = test.substring(j + 2).trim();

            Variable variable1 = functionVariable.getVariable(left, StringVariable.of(left));
            Variable variable2 = functionVariable.getVariable(right, StringVariable.of(right));
            return !variable1.getStringValue().equals(variable2.getStringValue());

        } else {
            return false;
        }

    }
}
