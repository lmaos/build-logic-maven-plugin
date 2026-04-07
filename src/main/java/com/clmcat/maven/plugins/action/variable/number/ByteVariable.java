package com.clmcat.maven.plugins.action.variable.number;

import com.clmcat.maven.plugins.action.Variable;


public class ByteVariable extends Variable.AbstractVariable<Byte> {
    public ByteVariable(Byte value) {
        super(value);
    }

    public static ByteVariable of(Byte value) {
        return new ByteVariable(value);
    }

    public static ByteVariable of(String value) {
        return new ByteVariable(Byte.parseByte(value));
    }

    public static ByteVariable of(Number value) {
        return new ByteVariable(value.byteValue());
    }
}
