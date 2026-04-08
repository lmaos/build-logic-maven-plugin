package com.clmcat.maven.plugins.action;

import java.util.ArrayList;
import java.util.List;

public interface GroupAction extends Action {

    void addAction(Action action);

    List<Action> getActions();


    public static abstract class AbstractGroupAction extends AbstractAction implements GroupAction
    {
        private List<Action> actions = new ArrayList<Action>();

        @Override
        public void addAction(Action action) {
            actions.add(action);
        }
        @Override
        public List<Action> getActions() {
            return actions;
        }

        public void setActions(List<Action> actions) {
            this.actions = actions;
        }

        @Override
        protected final void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

            callGroupExecute(actionParam, parentAction, actions);
        }

        // 组执行
        protected void callGroupExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            if (actions.isEmpty()) {
                emptyChildrenExecute(actionParam, parentAction);
            }
            for (Action action : actions) {
                action.execute(actionParam, this);
            }
        }

        /**
         * 当 Action 触发时为空时，默认执行操作：打印值。 方便查看 Action 实例的值
         *
         * @param actionParam 全局参数
         * @param parentAction 上层调用的 Action 实例
         * @throws Exception 执行异常
         */
        protected void emptyChildrenExecute(ActionParam actionParam, Action parentAction) throws Exception {
            String value = getValue();
            if (value != null) {
                String test = getTest();
                if (test != null) {
                    actionParam.info("<" + getTag() + " test="+test + ">" + value + "</" + getTag() + ">");
                } else {
                    actionParam.info("<" + getTag() + ">" + value + "</" + getTag() + ">");
                }
            }
        }


    }


}
