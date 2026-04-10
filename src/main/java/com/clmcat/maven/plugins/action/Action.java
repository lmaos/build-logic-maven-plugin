package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.factory.ActionFactory;
import com.clmcat.maven.plugins.action.factory.PlexusConfigurationAware;
import com.clmcat.maven.plugins.action.variable.FunctionActionVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface Action {
    void setTag(String tag);
    void setTagMethod(String method);
    void setValue(String value);
    void execute(ActionParam actionParam, Action parentAction) throws Exception ;


    public abstract static class AbstractAction implements Action, PlexusConfigurationAware {
        private String value;
        private String test;
        private String tag;
        private String description;
        private boolean enableTest = true;
        private String tagMethod;
        private PlexusConfiguration plexusConfiguration;

        @Override
        public void setPlexusConfiguration(PlexusConfiguration plexusConfiguration) {
            this.plexusConfiguration = plexusConfiguration;
        }

        public PlexusConfiguration getPlexusConfiguration() {
            return plexusConfiguration;
        }

        public void setEnableTest(boolean enableTest) {
            this.enableTest = enableTest;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void setTag(String tag) {
            this.tag = tag;

        }

        public String getTag() {
            return tag;
        }

        @Override
        public void setTagMethod(String tagMethod) {
            this.tagMethod = tagMethod;
        }

        public String getTagMethod() {
            return tagMethod;
        }

        public String getValue() {
            return FunctionVariablesReference.format(value);
        }

        public String getDescription() {
            return description;
        }

        public String getTest() {
            return FunctionVariablesReference.format(test);
        }

        /**
         * Execute the action. Default call order: initExecute → triggerExecute
         */
        @Override
        public final void execute(ActionParam actionParam, Action parentAction) throws Exception {
            if (description != null) {
                actionParam.info("Action [" + tag + "] execute -> [" + description + "]");
            }
            if (!initExecute(actionParam, parentAction)) {
                actionParam.warn("Action [" + tag + "] init status -> [skip execute]");
                return;
            };
            if (!enableTest || actionParam.test(getTest())) {
                triggerExecute(actionParam, parentAction);
            }
        }

        /**
         * Trigger execution. Default internal call order: before → callExecute → after
         * @param actionParam global action parameters
         * @param parentAction the parent Action instance that invoked this action
         * @throws Exception execution exception
         */
        protected void triggerExecute(ActionParam actionParam, Action parentAction) throws Exception {
            beforeExecute(actionParam, parentAction); // called before execution
            callExecute(actionParam, parentAction);
            afterExecute(actionParam, parentAction);  // called after execution
        }

        protected boolean initExecute(ActionParam actionParam, Action parentAction) throws Exception {
            return true;
        }

        protected void beforeExecute(ActionParam actionParam, Action parentAction) throws Exception {

        }

        protected void afterExecute(ActionParam actionParam, Action parentAction) throws Exception {

        }

        protected abstract void callExecute(ActionParam actionParam, Action action) throws Exception ;


        public FunctionVariables getFunctionVariables() {
            return FunctionVariablesReference.getFunctionVariables();
        }

        public FunctionVariable getThisFunctionVariable() {
            return FunctionVariablesReference.getFunctionVariables().getFirst();
        }

        public Variable getVariable(String name) {
            return getFunctionVariables().getVariable(name);
        }

        public Variable getVariable(String name, Variable defaultValue) {
            return getFunctionVariables().getVariable(name, defaultValue);
        }

        public Variable getThisVariable(String name) {
            return getThisFunctionVariable().getVariable(name);
        }

        /**
         * Set a variable in the specified scope.
         * @param actionParam global action parameters
         * @param scope variable scope (global, root, this, or default)
         * @param name variable name; if empty, no variable will be set
         * @param variable variable value
         */
        protected void setVariable(ActionParam actionParam, String scope, String name, Variable variable) {
            if (XUtils.isEmpty(name)) {
                actionParam.warn("Action <" + tag + " name=\""+name+"\"> name is empty; not set");
                return;
            }
            if ("global".equals(scope)) {
                actionParam.setVariable(name, variable);
            } else if ("root".equals(scope)) {
                getFunctionVariables().setRootVariable(name, variable);
            } else if ("this".equals(scope)) {
                getThisFunctionVariable().setVariable(name, variable);
            } else {
                getFunctionVariables().setVariable(name, variable);
            }
        }

        protected File getSafeDir(ActionParam actionParam) {
            Variable<File> var = actionParam.getVariable("allowWriteDir");
            if (var == null || !var.isExist() || var.getValue() == null) {
                return null; // no allowlist → deny
            }
            File allowDir = var.getValue();
            return allowDir;
        }

        protected boolean safeDir(ActionParam actionParam, File optFile) throws Exception {
            // null value is rejected immediately
            if (optFile == null) {
                return false;
            }

            // Resolve the canonical path (prevents ../ path-traversal attacks)
            String canonicalPath = optFile.getCanonicalPath().toLowerCase();

            // ====================== 1. High-risk system directories are blocked ======================
            if (canonicalPath.equals("/")
                    || canonicalPath.matches("^[a-z]:\\\\?$")  // C:\ D:\
                    || canonicalPath.contains("windows")
                    || canonicalPath.contains("system32")
                    || canonicalPath.contains("/bin")
                    || canonicalPath.contains("/sbin")
                    || canonicalPath.contains("/usr")
                    || canonicalPath.contains("/etc")) {
                return false;
            }

            // ====================== 2. Path must be within the allowlisted directory ======================
            Variable<File> var = actionParam.getVariable("allowWriteDir");
            if (var == null || !var.isExist() || var.getValue() == null) {
                return false; // no allowlist → deny
            }

            File allowDir = var.getValue();
            String allowCanonicalPath = allowDir.getCanonicalPath().toLowerCase();

            // Path must start with the allowlisted directory
            return canonicalPath.startsWith(allowCanonicalPath);
        }
        protected void callFunction(String name,ActionParam actionParam, Action parentAction) throws Exception {
            Variable variable = actionParam.getVariable("function:" + name);
            if (variable instanceof FunctionActionVariable) {

                ((FunctionActionVariable) variable).execute(actionParam, parentAction);
            }
        }

        // Parse child actions
        protected List<Action> parseChildren(ActionFactory actionFactory) throws Exception {
            List<Action>  children = new ArrayList<>();
            for (PlexusConfiguration child : plexusConfiguration.getChildren()) {
                Action action = actionFactory.newInstance(child);
                children.add(action);
            }
            return children;
        }
    }
}
