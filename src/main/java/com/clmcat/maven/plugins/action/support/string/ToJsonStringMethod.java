package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.StringVariable;

public class ToJsonStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {

        if (variable instanceof StringVariable) {
            return StringVariable.of(variable.getStringValue());
        }

        return StringVariable.of(XUtils.toJsonString(variable.getValue()));
    }
}
