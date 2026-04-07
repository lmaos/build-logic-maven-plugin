package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.util.ArrayList;
import java.util.List;

public class ListVariable<T> extends Variable.AbstractVariable<List<T>> {
    public ListVariable() {
        super(new ArrayList<T>());
    }

    public ListVariable(List<T> value) {
        super(value);
    }

    public void add(T value) {
        getValue().add(value);
    }

    public void addAll(List<T> value) {
        getValue().addAll(value);
    }

    public void clear() {
        getValue().clear();
    }

    public T get(int index) {
        return getValue().get(index);
    }

    public int size() {
        return getValue().size();
    }

    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean isExist() {
        return getValue() != null && getValue().size() > 0;
    }

    public void remove(int index) {
        getValue().remove(index);
    }

    @Override
    public List<T> getValue() {
        return super.getValue();
    }
}
