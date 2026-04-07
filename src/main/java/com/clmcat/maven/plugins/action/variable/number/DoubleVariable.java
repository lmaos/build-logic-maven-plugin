package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class DoubleVariable extends Variable.AbstractVariable<Double> {
    public DoubleVariable(Double value) {
        super(value);
    }

    public static DoubleVariable of(Double value) {
        return new DoubleVariable(value);
    }

    public static DoubleVariable of(String value) {
        return new DoubleVariable(Double.parseDouble(value));
    }

    public static DoubleVariable of(Number value) {
        return new DoubleVariable(value.doubleValue());
    }
}
