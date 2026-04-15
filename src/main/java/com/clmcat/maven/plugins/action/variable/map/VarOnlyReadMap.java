package com.clmcat.maven.plugins.action.variable.map;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;

import java.util.AbstractMap;
import java.util.Set;

public  class VarOnlyReadMap extends AbstractMap<String, Object> {
        private FunctionVariable functionVariable;
        public VarOnlyReadMap(FunctionVariable functionVariable) {
            this.functionVariable = functionVariable;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException("不支持 entrySet 操作");
        }

        @Override
        public Object get(Object key) {
            Variable variable = functionVariable.getVariable(String.valueOf(key));
            return !Variable.isNULL(variable) ? variable.getValue() : null;
        }

        @Override
        public Object getOrDefault(Object key, Object defaultValue) {
            Object value = get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

    }