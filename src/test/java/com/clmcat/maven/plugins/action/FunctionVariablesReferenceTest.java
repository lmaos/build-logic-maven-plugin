package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.compare.DefaultCompare;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FunctionVariablesReferenceTest {

    @AfterEach
    void clearThreadLocalStack() {
        FunctionVariables functionVariables = FunctionVariablesReference.getFunctionVariables();
        if (functionVariables != null) {
            functionVariables.clear();
        }
    }

    @Test
    void shouldResolveNestedScopesAndRestoreStack() throws Exception {
        FunctionVariablesReference.execute(null, outer -> {
            outer.setVariable("name", StringVariable.of("outer"));

            ActionParam outerParam = new ActionParam(null, null, null, new DefaultCompare(), outer);
            assertEquals("hello outer", outerParam.format("hello ${name}"));
            assertEquals("hello outer", FunctionVariablesReference.format("hello ${name}"));
            assertEquals(1, FunctionVariablesReference.getFunctionVariables().size());

            FunctionVariablesReference.execute(null, inner -> {
                inner.setVariable("name", StringVariable.of("inner"));

                ActionParam innerParam = new ActionParam(null, null, null, new DefaultCompare(), inner);
                assertEquals("hello inner", innerParam.format("hello ${name}"));
                assertEquals("hello inner", FunctionVariablesReference.format("hello ${name}"));
                assertEquals(2, FunctionVariablesReference.getFunctionVariables().size());
            });

            assertEquals(1, FunctionVariablesReference.getFunctionVariables().size());
            assertEquals("hello outer", FunctionVariablesReference.format("hello ${name}"));
        });

        FunctionVariables functionVariables = FunctionVariablesReference.getFunctionVariables();
        assertNotNull(functionVariables);
        assertTrue(functionVariables.isEmpty());
    }
}
