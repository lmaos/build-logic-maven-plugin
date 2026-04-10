package com.clmcat.maven.plugins.action.compare;

import com.clmcat.maven.plugins.action.compare.impl.*;
import com.clmcat.maven.plugins.action.compare.match.MatchResult;
import com.clmcat.maven.plugins.action.compare.match.MatchTree;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;

import java.util.Stack;

public class LogicCompare implements Compare {


    private MatchTree  logicMatchTree = MatchTree.getLogicMatchTree();
    private MatchTree  symbolMatchTree = MatchTree.getSymboMatchTree();


    @Override
    public boolean compare(String test, FunctionVariable functionVariable) {

        if (test == null || (test = test.trim()).isEmpty()) {
            return true;
        }
        Stack<CompareNodeGroup> stack = new Stack<>();
        char prevChar = ' ';

        CompareNodeGroup root = new CompareNodeGroup();
        CompareNodeGroup currentNode = root;

        int start = 0;
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            if ( prevChar != '\\' && (c == '(' || c == ')')) {
                if (c == '(') {
                    parseCompareNode(test, start, i, currentNode);
                    stack.push(currentNode);
                    start = i + 1;
                    CompareNodeGroup logicNode = new CompareNodeGroup();
                    currentNode.addCompareNode(logicNode);
                    currentNode = logicNode;
                } else if(stack.empty()) {
                    throw new IllegalArgumentException("logic compare error, not match ( and ), index: " + i);
                } else {
                    parseCompareNode(test, start, i, currentNode);
                    start = i + 1;
                    currentNode = stack.pop();
                }
            }
            if (currentNode == null) {
                throw new IllegalArgumentException("logic compare error, not match ( and ), index: " + i);
            }
            prevChar = c;
        }

        parseCompareNode(test, start, test.length(), currentNode);

        return root.evaluate(new VariableNodeParam(functionVariable));
    }

    private void parseCompareNode(String test, int start, int end, CompareNodeGroup currentNode) {

        if (start == end) {
            return;
        }
        int logicStart = start;
        int logicEnd = start;

            // &&， ||
        MatchResult logicResult = logicMatchTree.matchFristResult(test, start, end);
        if (logicResult != null) { // a logic operator was matched
            logicEnd = logicResult.getStart(); // the operator's start position is the logic end position. e.g. "xxx==ccc &&" -> "xxx==ccc"
        } else {
            logicEnd = end;
        }
        // == ,!=, >=, <=, > , <
        MatchResult symbolResult = symbolMatchTree.matchFristResult(test, logicStart, logicEnd);

        if (symbolResult == null) {
            // no comparison operator found: treat the token as a variable or boolean value
            String field = test.substring(logicStart, logicEnd).trim();
            if (!field.isEmpty()) {
                FieldElement fieldElement = new FieldElement(field);
                currentNode.addCompareNode(fieldElement);
            }
        } else {
            int symbolStart = symbolResult.getStart();
            int symbolLength = symbolResult.getLength();
            String left = test.substring(logicStart, symbolStart).trim();
            String right = test.substring(symbolStart + symbolLength, logicEnd).trim();
            String symbol = symbolResult.getMatch();
            EvaluaeElement compareElement = new EvaluaeElement(left, right, symbol);
            currentNode.addCompareNode(compareElement);
        }
        if (logicResult != null) {
            LogicSymbolNode logicSymbolNode = new LogicSymbolNode(logicResult.getMatch());
            currentNode.addCompareNode(logicSymbolNode);
            parseCompareNode(test, logicResult.getEnd(), end, currentNode);
        }

    }



}
