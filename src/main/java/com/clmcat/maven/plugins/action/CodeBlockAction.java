package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.variable.*;

import java.util.List;

public interface CodeBlockAction extends Action {

    void codeBlockExecute(ActionParam actionParam, Action parentAction) throws Exception;

    abstract class AbstractCodeBlockAction extends GroupAction.AbstractGroupAction implements CodeBlockAction {

        /**
         * Overrides triggerExecute to push a new code-block scope frame first.
         * Call order: triggerExecute → codeBlockExecute, with a new thread-local variable scope pushed.
         * @param actionParam global action parameters
         * @param parentAction the parent Action instance that invoked this action
         * @throws Exception code-block execution exception
         */
        @Override
        public final void triggerExecute(ActionParam actionParam, Action parentAction) throws Exception {
            FunctionVariablesReference.execute(this, (functionVariable) -> {
                codeBlockExecute(actionParam, parentAction);
            });
        }

        /**
         * Execute the code block. The default implementation delegates to super.triggerExecute.
         * Call order: codeBlockExecute → triggerExecute.
         *
         * <br>
         * If you override this method, call super.codeBlockExecute(actionParam, parentAction) to retain
         * the default behaviour; otherwise beforeExecute and afterExecute will not be called automatically.
         * <br>
         * It is recommended to override callCodeBlockExecute for custom business logic.
         *
         * @param actionParam global action parameters
         * @param parentAction the parent Action instance that invoked this action
         * @throws Exception execution exception
         */
        @Override
        public void codeBlockExecute(ActionParam actionParam, Action parentAction) throws Exception {
            super.triggerExecute(actionParam, parentAction);
        }
        /**
         * Overrides callGroupExecute to delegate to callCodeBlockExecute.
         * Call order: callGroupExecute → callCodeBlockExecute
         */
        @Override
        protected final void callGroupExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            this.callCodeBlockExecute(actionParam, parentAction, actions);
        }

        /**
         * Execute the code block. Current call order: {beforeExecute} → callCodeBlockExecute (here) → {afterExecute}.
         * The default implementation calls super.callGroupExecute, which executes each child action in sequence.
         *
         * @param actionParam global action parameters
         * @param parentAction parent caller
         * @param actions child actions
         * @throws Exception code-block execution exception
         */
        protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            super.callGroupExecute(actionParam, parentAction, actions);
        }

    }
}
