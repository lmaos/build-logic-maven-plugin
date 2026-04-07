package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class ShortVariable extends Variable.AbstractVariable<Short> {
    public ShortVariable(Short value) {
        super(value);
    }

    public static ShortVariable of(Short value) {
        return new ShortVariable(value);
    }

    public static ShortVariable of(String value) {
        return new ShortVariable(Short.parseShort(value));
    }

    public static ShortVariable of(Number value) {
        return new ShortVariable(value.shortValue());
    }
}
