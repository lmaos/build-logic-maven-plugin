package com.clmcat.maven.plugins.action.compare.impl;


import java.util.ArrayList;
import java.util.List;

public class CompareNodeGroup implements EvaluateNode {


    private List<CompareNode> compareNodes = new ArrayList<CompareNode>();

    @Override
    public boolean evaluate(NodeParam nodeParam) {

        boolean result = true;
        LogicSymbolNode logicNode = new LogicSymbolNode("&&");
        for (CompareNode compareNode : compareNodes) {

            if (compareNode instanceof EvaluateNode) {
                if (logicNode != null) {
                    if (logicNode.getLogic().equals("&&")) {
                        result = result && ((EvaluateNode) compareNode).evaluate(nodeParam);
                    } else {
                        result = result || ((EvaluateNode) compareNode).evaluate(nodeParam);
                    }
                    logicNode = null;
                } else {
                    throw new IllegalArgumentException("CompareNodeGroup must start with LogicNode");
                }
            }  else if (compareNode instanceof LogicSymbolNode) {
                logicNode =  (LogicSymbolNode) compareNode;
            } else {
                throw new IllegalArgumentException("Not support node type : " + compareNode.getClass().getSimpleName());
            }

        }
        return result;
    }

    public void addCompareNode(CompareNode compareNode) {
        compareNodes.add(compareNode);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (compareNodes.size() > 0) {
            for (CompareNode compareNode : compareNodes) {
                sb.append(compareNode.toString());
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }
}
