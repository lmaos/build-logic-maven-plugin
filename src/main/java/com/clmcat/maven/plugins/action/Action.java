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
           执行操作： 默认调用顺序: initExecute → triggerExecute

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
         * !触发执行操作, 默认内部调用顺序： before → callExecute → after
         * @param actionParam 全局参数
         * @param parentAction 上层调用的 Action 实例
         * @throws Exception
         */
        protected void triggerExecute(ActionParam actionParam, Action parentAction) throws Exception {
            beforeExecute(actionParam, parentAction); // 调用之前
            callExecute(actionParam, parentAction);
            afterExecute(actionParam, parentAction);  // 调用之后
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



        protected File getSafeDir(ActionParam actionParam) {
            Variable<File> var = actionParam.getVariable("allowWriteDir");
            if (var == null || !var.isExist() || var.getValue() == null) {
                return null; // 没有白名单 → 禁止
            }
            File allowDir = var.getValue();
            return allowDir;
        }

        protected boolean safeDir(ActionParam actionParam, File optFile) throws Exception {
            // 空值直接拒绝
            if (optFile == null) {
                return false;
            }

            // 获取真实路径（解决 ../ 穿透问题）
            String canonicalPath = optFile.getCanonicalPath().toLowerCase();

            // ====================== 1. 系统高危目录禁止操作 ======================
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

            // ====================== 2. 必须在白名单目录内 ======================
            Variable<File> var = actionParam.getVariable("allowWriteDir");
            if (var == null || !var.isExist() || var.getValue() == null) {
                return false; // 没有白名单 → 禁止
            }

            File allowDir = var.getValue();
            String allowCanonicalPath = allowDir.getCanonicalPath().toLowerCase();

            // 必须以白名单路径开头
            return canonicalPath.startsWith(allowCanonicalPath);
        }
        protected void callFunction(String name,ActionParam actionParam, Action parentAction) throws Exception {
            Variable variable = actionParam.getVariable("function:" + name);
            if (variable instanceof FunctionActionVariable) {

                ((FunctionActionVariable) variable).execute(actionParam, parentAction);
            }
        }

        // 解析子Action
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
