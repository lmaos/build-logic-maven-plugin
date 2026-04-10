package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.BooleanVariable;

/**
 * A value: true|false, or a variable name.
 * @author zhangxingyu
 * @since 1.0.0
 *
 */
public class FieldElement implements EvaluateNode {
    private String field;
    public FieldElement(String field) {
        this.field = field;
    }
    @Override
    public boolean evaluate(NodeParam nodeParam) {
        String _field = field.trim();
        if (_field.isEmpty()) {
            throw  new IllegalArgumentException("field is empty");
        }
        boolean flag = false;
        if (_field.charAt(0) == '!') {
            flag = true;
            _field = _field.substring(1);
        }
        if (flag) {
            return !evaluate(_field, nodeParam);
        } else {
            return evaluate(_field, nodeParam);
        }
    }

    public boolean evaluate(String field, NodeParam nodeParam) {

        String _field = field.trim();

        Variable variable = nodeParam.getVariable(_field);
        // Check whether the variable exists; if not, check if it is a boolean literal, otherwise return false.
        if (variable == null || !variable.isExist()) {
            // strip surrounding quotes
            if (_field.startsWith("\"") && _field.endsWith("\"")
                    || _field.startsWith("'") && _field.endsWith("'")) {
                _field = _field.substring(1, _field.length() - 1);
            }

            if ("true".equals(_field)) {
                return true;
            }  else if ("false".equals(_field)) {
                return false;
            }

            return false;
        }
        // If the variable is a Boolean type, return its value directly
        if (variable instanceof BooleanVariable || variable.getValue() instanceof Boolean) {
            return (Boolean) variable.getValue();
        }

        String stringValue = variable.getStringValue();
        // If the variable is a String type, evaluate based on its string value
        if ("true".equals(stringValue)) {
            return true;
        } else if ("false".equals(stringValue)) {
            return false;
        }
        // If the variable exists, it is truthy
        return true;
    }

    @Override
    public String toString() {
        return field;
    }
}
