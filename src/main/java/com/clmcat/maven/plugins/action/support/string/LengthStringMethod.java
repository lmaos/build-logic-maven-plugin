package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.number.IntVariable;

public class LengthStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String stringValue = variable.getStringValue();
        return IntVariable.of(stringValue.length());
    }
}
