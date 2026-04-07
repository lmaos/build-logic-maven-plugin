package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.StringVariable;

public class CharAtStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String tag = stringAction.getTag();
        String name = stringAction.getName();
        if (params == null || params.length == 0) {
            throw new Exception("<" + tag + " name = " + name + "> charAt params is null");
        }
        String stringValue = variable.getStringValue();
        return StringVariable.of(stringValue.charAt(Integer.parseInt(params[0])));
    }
}
