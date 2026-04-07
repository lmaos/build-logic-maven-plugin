package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class FloatVariable extends Variable.AbstractVariable<Float> {
    public FloatVariable(Float value) {
        super(value);
    }

    public static FloatVariable of(Float value) {
        return new FloatVariable(value);
    }

    public static FloatVariable of(String value) {
        return new FloatVariable(Float.parseFloat(value));
    }

    public static FloatVariable of(Number value) {
        return new FloatVariable(value.floatValue());
    }
}
