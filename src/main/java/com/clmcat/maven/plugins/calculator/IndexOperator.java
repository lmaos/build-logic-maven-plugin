package com.clmcat.maven.plugins.calculator;

/**
 * 下标访问运算符定义。
 */
final class IndexOperator {

    static final String SYMBOL = "[]";

    private IndexOperator() {
    }

    static RuntimeValue apply(RuntimeValue target, RuntimeValue index) {
        return ExpressionRuntimeSupport.indexAccess(target, index);
    }
}
