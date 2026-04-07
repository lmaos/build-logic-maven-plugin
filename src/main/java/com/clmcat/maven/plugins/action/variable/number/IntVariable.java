package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class IntVariable extends Variable.AbstractVariable<Integer> {
    public IntVariable(Integer value) {
        super(value);
    }

    public static IntVariable of(Integer value) {
        return new IntVariable(value);
    }

    public static IntVariable of(String value) {
        return new IntVariable(Integer.parseInt(value));
    }

    public static IntVariable of(Number value) {
        return new IntVariable(value.intValue());
    }
}
