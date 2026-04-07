package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapVariable<K,V> extends Variable.AbstractVariable<Map<K,V>> {
    public MapVariable() {
        super(new HashMap<>());
    }

    public MapVariable(Map<K, V> value) {
        super(value);
    }


    public void put(K key, V value) {
        getValue().put(key, value);
    }

    public void clear() {
        getValue().clear();
    }

    public V get(String name) {
        return getValue().get(name);
    }

    public void putAll(Map<K, V> map) {
        getValue().putAll(map);
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

    @Override
    public Map<K, V> getValue() {
        return super.getValue();
    }

    public static <K,V> MapVariable<K,V> of(Map<K, V> value) {
        return new MapVariable<>(value);
    }



}
