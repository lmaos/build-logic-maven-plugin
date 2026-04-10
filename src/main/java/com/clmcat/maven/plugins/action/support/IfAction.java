package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.CodeBlockAction;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;

/*
 <if test="condition">
  <then >
    <code block>
  </then>
  <elseif test="condition">
    <code block>
  </elseif>
  <else>
    <code block>
  </else>
 </if>
 */
public class IfAction extends CodeBlockAction.AbstractCodeBlockAction  {
    public IfAction() {
        setEnableTest(false);
    }


        @Override
        protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
        String test = getTest();
        if (test == null || test.trim().isEmpty()) {
            throw new MojoExecutionException("if action must have test attribute， <if test=\"condition\">");
        }
        ActionFactory actionFactory = actionParam.getActionExecute().getActionFactory().copy();
        actionFactory.addActionType("if", IfAction.class);
        actionFactory.addActionType("elseif", ElseIfAction.class);
        actionFactory.addActionType("else", ElseAction.class);
        actionFactory.addActionType("then", ThenAction.class);

        actions = parseChildren(actionFactory);

        if (actions == null || actions.isEmpty()) {
            throw new MojoExecutionException("if action must have <then>, <elseif>, <else> actions");
        }

        ThenAction thenAction = null;
        List<ElseIfAction> elseIfActions = new ArrayList<>();
        ElseAction elseAction = null;
        for (Action action : actions) {
            if (action instanceof ThenAction) {
                if (thenAction != null) {
                    throw new MojoExecutionException("if action must have only one <then> action");
                }
                thenAction = (ThenAction) action;
            } else if (action instanceof ElseAction) {
                if (elseAction != null) {
                    throw new MojoExecutionException("if action must have only one <else> action");
                }
                elseAction = (ElseAction) action;
            } else if  (action instanceof ElseIfAction) {
                // <elseif test="condition"> must have a test attribute
                if (((ElseIfAction) action).getTest() == null || ((ElseIfAction) action).getTest().trim().isEmpty()) {
                    throw new MojoExecutionException("elseif action must have test attribute， <elseif test=\"condition\">");
                }
                elseIfActions.add((ElseIfAction) action);
            } else { // only <then>, <elseif>, <else> are supported
                throw new MojoExecutionException("if action must have only <then>, <elseif>, <else> actions");
            }

        }
        if (actionParam.test(test)) {
            thenAction.execute(actionParam, this);
        } else {
            for (ElseIfAction elseIfAction : elseIfActions) {
                if (actionParam.test(elseIfAction.getTest())) {
                    elseIfAction.execute(actionParam, this);
                    return;
                }
            }
            if (elseAction != null) {
                elseAction.execute(actionParam, this);
            }
        }


    }

    public static class ElseAction extends CodeBlockAction.AbstractCodeBlockAction {
        public ElseAction() {
            setEnableTest(false);
        }
    }

    public static class ElseIfAction extends CodeBlockAction.AbstractCodeBlockAction {
        public ElseIfAction() {
            setEnableTest(false);
        }
    }

    public static class ThenAction extends CodeBlockAction.AbstractCodeBlockAction {
        public ThenAction() {
            setEnableTest(false);
        }
    }
}
