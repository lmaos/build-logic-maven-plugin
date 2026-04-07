package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.BooleanVariable;

/**
 * 值： true|false, 或 变量名。
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
        // 验证变量是否存在。不存在， 验证是否是 boolean 类型，不是则直接返回false。
        if (variable == null || !variable.isExist()) {
            // 去掉引号
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
        // 如果变量是Boolean类型，则直接返回变量值
        if (variable instanceof BooleanVariable || variable.getValue() instanceof Boolean) {
            return (Boolean) variable.getValue();
        }

        String stringValue = variable.getStringValue();
        // 如果变量是字符串类型，则根据字符串值判断是否为true或false
        if ("true".equals(stringValue)) {
            return true;
        } else if ("false".equals(stringValue)) {
            return false;
        }
        // 变量存在，则也是真即可。
        return true;
    }

    @Override
    public String toString() {
        return field;
    }
}
