package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.BooleanVariable;
import com.clmcat.maven.plugins.action.variable.NumberVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.math.BigDecimal;


public class VariableAction extends Action.AbstractAction {

    private String name;
    private String scope;
    private String ref; // 引用其他变量
    private Variable storeVariable;

    public void setName(String name) {
        this.name = name;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public String getScope() {
        return scope;
    }

    public String getRef() {
        return ref;
    }

    @Override
    protected boolean initExecute(ActionParam actionParam, Action parentAction) throws Exception {
        if (name == null) {
            throw new IllegalArgumentException("any variable must have name attribute; <" + getTag() + " name=\"name\" />");
        }
        // 严格匹配：只能是 $ 0-9 a-z A-Z，且不能为空
        if (!name.matches("^[$0-9a-zA-Z_]+$")) {
            throw new MojoExecutionException("any variable name must only contain $, 0-9, a-z, A-Z,_ (no dots allowed): <" + getTag() + " name=\"" + name + "\" />");
        }
        return super.initExecute(actionParam, parentAction);
    }

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {


        if (ref != null && !(ref = ref.trim()).isEmpty()) {
            Variable refVariable = actionParam.getVariable(ref);
            if (Variable.isNULL(refVariable)) {
                throw new MojoExecutionException("Variable ref must exist: <var ref=\"" + ref + "\" />");
            }
            setVariable(actionParam, refVariable);
        } else {

            String value = actionParam.format(getValue());

            Variable variable = null;
            if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
                variable = new StringVariable(value);
            } else if (isNumber(value)) {
                variable = new NumberVariable(new BigDecimal(value));
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                variable = new BooleanVariable("true".equalsIgnoreCase(value));
            } else {
                variable = new StringVariable(value);
            }


            setVariable(actionParam, variable);
        }
    }

    protected final void setVariable(ActionParam actionParam, Variable variable) {
        setVariable(actionParam, name, variable);
    }

    protected final void setVariable(ActionParam actionParam, String name, Variable variable) {
        if ("global".equals(scope)) {
            actionParam.setVariable(name, variable);
        } else if ("root".equals(scope)) {
            getFunctionVariables().setRootVariable(name, variable);
        } else if ("this".equals(scope)) {
            getThisFunctionVariable().setVariable(name, variable);
        } else {
            getFunctionVariables().setVariable(name, variable);
        }
        storeVariable = variable;
    }

    public Variable getStoreVariable() {
        return storeVariable;
    }

    private boolean isNumber(String text) {
        return XUtils.isNumber(text);
    }
}
