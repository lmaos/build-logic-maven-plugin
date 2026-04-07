package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.support.MainAction;
import com.clmcat.maven.plugins.action.variable.*;
import org.apache.maven.plugin.MojoExecutionException;

public interface FunctionAction extends GroupAction {
    /**
     * 触发函数执行
     * @param actionParam 全局参数实例
     * @param parentAction 父操作实例
     * @throws Exception 执行异常
     */
    void triggerFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception;

    boolean isAutoExecute() throws Exception;


    default void execute(ActionParam actionParam, Action parentAction) throws Exception {
        if (!isAutoExecute()) {
            return;
        }
        triggerFunctionExecute(actionParam, parentAction);
    }

    abstract class AbstractFunctionAction extends AbstractGroupAction implements FunctionAction {

        /**
         *
         * @param name 默认的函数名
         * @param autoExecute 是否自动执行
         */
        protected AbstractFunctionAction(String name, boolean autoExecute) {
            this.name = name;
            this.autoExecute = autoExecute;
        }

        /**
         *
         * @param autoExecute 是否自动执行
         */
        protected AbstractFunctionAction(boolean autoExecute) {

            this.autoExecute = autoExecute;
        }

        // 函数名
        private String name;
        private boolean autoExecute;


//        public FunctionVariable getFunctionVariable() {
//            return FunctionVariablesReference.getFunctionVariables();
//        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        protected final boolean initExecute(ActionParam actionParam, Action parentAction) throws Exception {
            if (parentAction != null && !(parentAction instanceof MainAction)) {

                throw new MojoExecutionException("Function can only be defined in <main> or root level");
            }
            String name = getFunctionName();
            // 函数名必填
            if (name == null || (name = name.trim()).isEmpty()) {
                throw new MojoExecutionException("Function name is required");
            }
            // 严格匹配：只能是 $ 0-9 a-z A-Z，且不能为空
            if (!name.matches("^[$0-9a-zA-Z]+$")) {
                throw new MojoExecutionException("Function name must only contain $, 0-9, a-z, A-Z (no dots allowed): <func name=\"" + name + "\" />");
            }

            // 如果不进行自动执行，将函数添加到变量中
            if (!isAutoExecute()) {
                if (actionParam.getVariable("function:" + name).isExist()) {
                    throw new MojoExecutionException("Function name " + name + " is already defined");
                }
                actionParam.setVariable("function:" + name, FunctionActionVariable.of(this));
                return false;
            }
            return initFunctionExecute(actionParam, parentAction);
        }

        protected boolean initFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception {
            return true;
        }

        @Override
        public final void triggerExecute(ActionParam actionParam, Action parentAction) throws Exception {
            triggerFunctionExecute(actionParam, parentAction);
        }

        @Override
        public final void triggerFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception {
            String name = getFunctionName();
            actionParam.debug("Start function execution: " + name);
            FunctionVariablesReference.execute(this, (functionVariable) -> {
                beforeFunctionExecute(actionParam, parentAction);
                try {
                    functionExecute(actionParam, parentAction);
                } catch (ReturnException e) {
                    if (parentAction != null) {
                        String returnName = "return." + getFunctionName();
                        FunctionVariables functionVariables = getFunctionVariables();
                        for (FunctionVariable variable : functionVariables) {
                            if (variable.getAction() == parentAction) {
                                variable.setVariable(returnName, e.getVariable());
                                break;
                            }
                        }
                    }
                }
                afterFunctionExecute(actionParam, parentAction);
            });
            actionParam.debug("End function execution: " + name);
        }

        protected String getFunctionName() {
            String name = getName();
            if (XUtils.isEmpty(name)) {
                return getTagMethod();
            } else  {
                return name;
            }
        }


        public void functionExecute(ActionParam actionParam, Action parentAction, Object... args) throws Exception {
            super.triggerExecute(actionParam, parentAction);
        }

        public void setAutoExecute(boolean autoExecute) {
            this.autoExecute = autoExecute; // 使用 this 关键字引用实例变量
        }

        @Override
        public boolean isAutoExecute()  {
            return autoExecute;
        }

        protected void beforeFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception {

        }

        protected void afterFunctionExecute(ActionParam actionParam, Action parentAction) throws Exception {

        }


    }


    public static class ReturnException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final Variable variable;

        public ReturnException(Variable variable) {
            this.variable = variable;
        }

        public Variable getVariable() {
            return variable;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return null;
        }
    }

}
