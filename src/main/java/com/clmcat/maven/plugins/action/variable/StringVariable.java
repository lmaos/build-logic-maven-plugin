package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

public class StringVariable extends Variable.AbstractVariable<String> {
    public StringVariable(String value) {
        super(value);
    }

    public static StringVariable of(String value) {
        return new StringVariable(value);
    }
}
