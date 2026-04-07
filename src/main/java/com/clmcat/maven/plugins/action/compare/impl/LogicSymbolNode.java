package com.clmcat.maven.plugins.action.compare.impl;

public class LogicSymbolNode implements CompareNode {

    private String logic;

    public LogicSymbolNode(String logic) {
        this.logic = logic;
    }

    public String getLogic() {
        return logic;
    }

    @Override
    public String toString() {
        return logic;
    }
}
