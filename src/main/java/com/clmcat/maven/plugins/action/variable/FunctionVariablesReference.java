package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.format.Format;

public class FunctionVariablesReference {
    private static final ThreadLocal<FunctionVariables> threadLocalFunctionVariables = new ThreadLocal<>();

    public static FunctionVariables getFunctionVariables() {
        return threadLocalFunctionVariables.get();
    }



    public static FunctionVariables getOrCreateFunctionVariables() {
        FunctionVariables functionVariables = threadLocalFunctionVariables.get();
        if (functionVariables == null) {
            functionVariables = new FunctionVariables();
            threadLocalFunctionVariables.set(functionVariables);
        }
        return functionVariables;
    }


    public static void execute(Action action,Executable executable) throws Exception {
        try {
            FunctionVariable functionVariable = new FunctionVariableItem(action);
            getOrCreateFunctionVariables().addFirst(functionVariable);
            executable.execute(functionVariable);
        }finally {
            getOrCreateFunctionVariables().removeFirst();
        }
    }


    public static interface Executable {
        void execute(FunctionVariable functionVariable) throws Exception;
    }

    public static String format(String text) {
        FunctionVariables functionVariables = FunctionVariablesReference.getFunctionVariables();

        return Format.formatString(text, functionVariables, "${?}");
    }
}
