package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

public class StringVariable extends Variable.AbstractVariable<String> {
    public StringVariable(String value) {
        super(value);
    }

    public static StringVariable of(String value) {
        return new StringVariable(value);
    }

    public static StringVariable ofNotNull(String value) {
        return new StringVariable(value == null ? "" : value);
    }
    public static StringVariable of(Object value) {
        return new StringVariable(String.valueOf(value));
    }
}
