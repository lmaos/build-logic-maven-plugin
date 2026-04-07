package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class LongVariable extends Variable.AbstractVariable<Long> {
    public LongVariable(Long value) {
        super(value);
    }

    public static LongVariable of(Long value) {
        return new LongVariable(value);
    }

    public static LongVariable of(String value) {
        return new LongVariable(Long.parseLong(value));
    }

    public static LongVariable of(Number value) {
        return new LongVariable(value.longValue());
    }
}
