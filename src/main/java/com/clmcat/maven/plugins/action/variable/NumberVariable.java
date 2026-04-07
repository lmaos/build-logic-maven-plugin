package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.math.BigDecimal;

public class NumberVariable extends Variable.AbstractVariable<BigDecimal> {
    public NumberVariable(BigDecimal value) {
        super(value);
    }

    public static NumberVariable of(BigDecimal value) {
        return new NumberVariable(value);
    }

    public static NumberVariable of(String value) {
        return new NumberVariable(new BigDecimal(value));
    }

    public static NumberVariable of(int value) {
        return new NumberVariable(new BigDecimal(value));
    }
}
