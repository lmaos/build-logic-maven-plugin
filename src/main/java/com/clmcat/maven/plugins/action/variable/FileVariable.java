package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.io.File;

public class FileVariable extends Variable.AbstractVariable<File> {

    public FileVariable(File value) {
        super(value);
    }

    @Override
    public boolean isExist() {
        return getValue() != null && getValue().exists();
    }

    public static FileVariable of(File value) {
        return new FileVariable(value);
    }
}
