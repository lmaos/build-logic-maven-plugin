package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.compare.impl.NodeParam;

public interface Variable<T> extends Comparable<Variable<T>> {

    public static final Variable NULL = new AbstractVariable(null) {};

    boolean isExist();

    T getValue();

    String getStringValue();


    public static boolean isNULL(Variable var) {
        return var == null || var == Variable.NULL || var.getValue() == null;
    }

    public static boolean isExist(Variable var) {
        return !isNULL(var) && var.isExist();
    }

    public abstract static class AbstractVariable<T> implements Variable<T> {
        private T value;

        public AbstractVariable(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean isExist() {
            return getValue() != null;
        }

        @Override
        public String getStringValue() {
            T value = getValue();
            return value == null ? null : value.toString();
        }

        @Override
        public String toString() {
            return getStringValue();
        }

        @Override
        public int compareTo(Variable<T> o) {
            // 1. Other side is null → self is greater, sort later
            if (o == null || !o.isExist() || o.getValue() == null) {
                return 1;
            }
            // 2. Self is null → self is smaller, sort earlier
            if (this.getValue() == null) {
                return -1;
            }

            // 3. Try value comparison (safe version)
            T thisVal = this.getValue();
            T thatVal = o.getValue();

            if (thisVal instanceof Comparable && thatVal instanceof Comparable) {
                return ((Comparable<T>) thisVal).compareTo(thatVal);
            }

            // 4. Fall back to string comparison (absolutely NPE-safe!)
            String s1 = this.getStringValue();
            String s2 = o.getStringValue();

            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;

            return s1.compareTo(s2);
        }

    }
}
