package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import com.clmcat.maven.plugins.action.variable.number.IntVariable;

public class SetStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String stringValue = variable.getStringValue();
        return StringVariable.of(stringValue);
    }
}
