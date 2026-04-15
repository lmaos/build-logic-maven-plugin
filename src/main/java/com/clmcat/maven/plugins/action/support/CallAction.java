package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.CodeBlockAction;
import com.clmcat.maven.plugins.action.DeferredChildrenParsingAction;
import com.clmcat.maven.plugins.action.factory.ActionFactory;

import java.util.List;

/**
 * 调用函数
 */
public  class CallAction extends CodeBlockAction.AbstractCodeBlockAction implements DeferredChildrenParsingAction {

    @Override
    public void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
        ActionFactory actionFactory = actionParam.getActionExecute().getActionFactory().create();
        actionFactory.addActionType("arg", ArgAction.class);
        actions = parseChildren(actionFactory);
        super.callCodeBlockExecute(actionParam, parentAction,  actions);
        callFunction(actionParam.format(getTagMethod()), actionParam, parentAction);
    }


    public static class ArgAction extends VariableAction {

        public ArgAction() {
            super();
            setScope("this");
        }

        @Override
        protected boolean initExecute(ActionParam actionParam, Action parentAction) throws Exception {

            if (parentAction instanceof CallAction) {


            } else {
                throw new IllegalArgumentException("ArgAction must be child of CallAction, <call> <arg /> </call>");
            }

            return super.initExecute(actionParam, parentAction);
        }
    }
}

