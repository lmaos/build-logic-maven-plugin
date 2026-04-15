package com.clmcat.maven.plugins.action.compare;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.map.VarOnlyReadMap;
import com.clmcat.maven.plugins.calculator.ExpressionCalculator;
import com.clmcat.maven.plugins.calculator.IterativeExpressionCalculator;

import java.util.*;

public class ExpressionCalculatorCompare implements Compare {

    private ExpressionCalculator expressionCalculator;

    public ExpressionCalculatorCompare(ExpressionCalculator expressionCalculator) {
        this.expressionCalculator = expressionCalculator;
    }

    public ExpressionCalculatorCompare() {
        this(new IterativeExpressionCalculator());
    }

    @Override
    public boolean compare(String test, FunctionVariable functionVariable) {
        if (XUtils.isEmpty(test)) {
            return true;
        }

        return expressionCalculator.compareCalculation(test, new VarOnlyReadMap(functionVariable));
    }



}
