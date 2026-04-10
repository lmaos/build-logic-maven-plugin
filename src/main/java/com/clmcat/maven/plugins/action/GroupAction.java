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

        // Group execute
        protected void callGroupExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            if (actions.isEmpty()) {
                emptyChildrenExecute(actionParam, parentAction);
            }
            for (Action action : actions) {
                action.execute(actionParam, this);
            }
        }

        /**
         * Default action when no child actions are present: print the value.
         * Useful for inspecting the value of an Action instance.
         *
         * @param actionParam global action parameters
         * @param parentAction the parent Action instance that invoked this action
         * @throws Exception execution exception
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
