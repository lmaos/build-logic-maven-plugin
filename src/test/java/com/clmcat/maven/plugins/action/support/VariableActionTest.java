package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.compare.DefaultCompare;
import com.clmcat.maven.plugins.action.variable.BooleanVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import com.clmcat.maven.plugins.action.variable.number.IntVariable;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VariableActionTest {

    @Test
    void shouldInferLiteralVariableTypes() throws Exception {
        FunctionVariablesReference.execute(null, scope -> {
            ActionParam actionParam = new ActionParam(null, null, null, new DefaultCompare(), scope);

            VariableAction typedAction = new VariableAction();
            typedAction.setName("answer");
            typedAction.setTagMethod("int");
            typedAction.setValue("42");
            typedAction.execute(actionParam, null);

            VariableAction quotedAction = new VariableAction();
            quotedAction.setName("message");
            quotedAction.setValue("'hello world'");
            quotedAction.execute(actionParam, null);

            VariableAction numberAction = new VariableAction();
            numberAction.setName("price");
            numberAction.setValue("12.5");
            numberAction.execute(actionParam, null);

            VariableAction booleanAction = new VariableAction();
            booleanAction.setName("enabled");
            booleanAction.setValue("true");
            booleanAction.execute(actionParam, null);

            assertTrue(actionParam.getVariable("answer") instanceof IntVariable);
            assertEquals("42", actionParam.getVariable("answer").getStringValue());
            assertTrue(actionParam.getVariable("message") instanceof StringVariable);
            assertEquals("hello world", actionParam.getVariable("message").getStringValue());
            assertTrue(actionParam.getVariable("price") instanceof NumberVariable);
            assertEquals("12.5", actionParam.getVariable("price").getStringValue());
            assertTrue(actionParam.getVariable("enabled") instanceof BooleanVariable);
            assertEquals("true", actionParam.getVariable("enabled").getStringValue());
        });
    }

    @Test
    void shouldRejectInvalidVariableName() throws Exception {
        FunctionVariablesReference.execute(null, scope -> {
            VariableAction action = new VariableAction();
            action.setName("bad.name");
            action.setValue("1");

            ActionParam actionParam = new ActionParam(null, null, null, new DefaultCompare(), scope);
            assertThrows(MojoExecutionException.class, () -> action.execute(actionParam, null));
        });
    }

    @AfterEach
    void clearThreadLocalStack() {
        FunctionVariables functionVariables = FunctionVariablesReference.getFunctionVariables();
        if (functionVariables != null) {
            functionVariables.clear();
        }
    }
}
