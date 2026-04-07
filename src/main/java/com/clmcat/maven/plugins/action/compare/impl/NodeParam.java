package com.clmcat.maven.plugins.action.compare.impl;

import com.clmcat.maven.plugins.action.Variable;

public interface NodeParam {


    /**
     * d
     * 读取节点参数
     * @param left 左节点变量名
     * @param right 右节点变量名
     * @return 节点变量实例
     */
    public NodeVariable getNodeVariable(String left, String right);

    /**
     * 返回变量，如果变量不存在，则返回 null
     * @param name 变量名
     * @return 变量实例
     */
    Variable getVariable(String name);

    /**
     * 读取变量值
     */
    interface NodeVariable {
        Variable getLeftVariable();
        Variable getRightVariable();
    }
}
