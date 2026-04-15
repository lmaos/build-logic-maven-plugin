package com.clmcat.maven.plugins.action.factory;

import com.clmcat.maven.plugins.action.support.*;
import com.clmcat.maven.plugins.action.support.codec.Base64Action;
import com.clmcat.maven.plugins.action.support.string.StringAction;

public final class ActionFactories {

    private ActionFactories() {
    }

    public static DefaultActionFactory createRootActionFactory() {
        DefaultActionFactory factory = new DefaultActionFactory();
        registerRootActions(factory);
        return factory;
    }

    public static void registerRootActions(ActionFactory actionFactory) {
        actionFactory.addActionType("main", MainAction.class);
        actionFactory.addActionType("echo", EchoAction.class);
        actionFactory.addActionType("var", VariableAction.class);
        actionFactory.addActionType("file", FileAction.class);
        actionFactory.addActionType("write", WriteAction.class);
        actionFactory.addActionType("read", ReadAction.class);
        actionFactory.addActionType("mkdir", MkdirAction.class);
        actionFactory.addActionType("zip", ZipAction.class);
        actionFactory.addActionType("delete", DeleteAction.class);
        actionFactory.addActionType("func", FuncAction.class);
        actionFactory.addActionType("call", CallAction.class);
        actionFactory.addActionType("return", ReturnAction.class);
        actionFactory.addActionType("if", IfAction.class);
        actionFactory.addActionType("foreach", ForEachAction.class);
        actionFactory.addActionType("list", ListAction.class);
        actionFactory.addActionType("http", HttpAction.class);
        actionFactory.addActionType("str", StringAction.class);
        actionFactory.addActionType("base64", Base64Action.class);
        actionFactory.addActionType("date", DateAction.class);
    }
}
