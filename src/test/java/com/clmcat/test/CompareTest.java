package com.clmcat.test;

import com.clmcat.maven.plugins.action.compare.Compare;
import com.clmcat.maven.plugins.action.compare.LogicCompare;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariableItem;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.junit.jupiter.api.Test;

public class CompareTest {
    @Test
    public void test() {

        FunctionVariable functionVariable = new FunctionVariableItem(null);

        functionVariable.setVariable("a", NumberVariable.of("12345"));
        functionVariable.setVariable("b", StringVariable.of("12345"));
        functionVariable.setVariable("c", StringVariable.of("7788"));

        Compare compare = new LogicCompare();

        String text = "a > 10";
        boolean result = compare.compare(text, functionVariable);
        System.out.println(text + ": " + result);
    }

}
