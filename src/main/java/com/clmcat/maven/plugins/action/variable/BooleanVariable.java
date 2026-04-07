package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

public class BooleanVariable extends Variable.AbstractVariable<Boolean> {
    public BooleanVariable(Boolean value) {
        super(value);
    }

    public static BooleanVariable of(Boolean value) {
        return new BooleanVariable(value);
    }
    public static BooleanVariable of(String value) {
        return new BooleanVariable("true".equalsIgnoreCase(value));
    }
}
