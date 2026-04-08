package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.variable.*;

import java.util.List;

public interface CodeBlockAction extends Action {

    void codeBlockExecute(ActionParam actionParam, Action parentAction) throws Exception;

    abstract class AbstractCodeBlockAction extends GroupAction.AbstractGroupAction implements CodeBlockAction {

        /**
         * 重写方法， 当前方法替换为调用代码块的执行。 triggerExecute → codeBlockExecute， 增加 代码块线程变量。
         * @param actionParam 全局参数
         * @param parentAction 上层调用的 Action 实例
         * @throws Exception 代码块执行异常
         */
        @Override
        public final void triggerExecute(ActionParam actionParam, Action parentAction) throws Exception {
            FunctionVariablesReference.execute(this, (functionVariable) -> {
                codeBlockExecute(actionParam, parentAction);
            });
        }

        /**
         * 代码块执行， 默认实现调用 super.triggerExecute。(调用顺序 → codeBlockExecute → triggerExecute)；
         *
         * <br>
         * 如果重写此方法， 在子实现中调用 super.codeBlockExecute(actionParam, parentAction) 保持默认行为。 否则 beforeExecute 和 afterExecute 不会主动执行。
         * <br>
         * 建议请重写: callCodeBlockExecute 实现子业务调用。
         *
         * @param actionParam 全局参数
         * @param parentAction 上层调用的 Action 实例
         * @throws Exception 执行异常
         */
        @Override
        public void codeBlockExecute(ActionParam actionParam, Action parentAction) throws Exception {
            super.triggerExecute(actionParam, parentAction);
        }
        /**
            重写方法， 当前方法替换为调用代码块的执行。 callGroupExecute → callCodeBlockExecute
         */
        @Override
        protected final void callGroupExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            this.callCodeBlockExecute(actionParam, parentAction, actions);
        }

        /**
         * 调用代码块的执行。 当前执行顺序: {beforeExecute} → callCodeBlockExecute (当前位置) → {afterExecute}； 默认实现调用 super.callGroupExecute，子Action依次调用
         *
         *
         * @param actionParam 全局参数
         * @param parentAction 父调用
         * @param actions 子Action
         * @throws Exception 代码块执行异常
         */
        protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            super.callGroupExecute(actionParam, parentAction, actions);
        }

    }
}
