package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.BooleanVariable;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import com.clmcat.maven.plugins.action.variable.VariableFactory;
import org.apache.maven.plugin.MojoExecutionException;

import java.math.BigDecimal;


public class VariableAction extends Action.AbstractAction {

    private String name;
    private String scope;
    private String ref; // reference to another variable
    private Variable storeVariable;
    // whether the name attribute is required
    private boolean nameRequired = true;

    public void setName(String name) {
        this.name = name;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setNameRequired(boolean nameRequired) {
        this.nameRequired = nameRequired;
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
        verifyName(actionParam, parentAction);
        return super.initExecute(actionParam, parentAction);
    }

    protected void verifyName(ActionParam actionParam, Action parentAction) throws Exception {
        if (nameRequired && XUtils.isEmpty(name)) {
            throw new IllegalArgumentException("any variable must have name attribute; <" + getTag() + " name=\"name\" />");
        }
        // Strict match: only $, 0-9, a-z, A-Z, _ are allowed; must not be empty
        if (name != null && !XUtils.isVariableName(name)) {
            throw new MojoExecutionException("any variable name must only contain $, 0-9, a-z, A-Z,_ (no dots allowed): <" + getTag() + " name=\"" + name + "\" />");
        }
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

            if (!XUtils.isEmpty(getTagMethod())) {
                variable = VariableFactory.newVariable(getTagMethod(), value);
            } else if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
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
        setVariable(actionParam, scope, name, variable);
    }


    @Override
    protected final void setVariable(ActionParam actionParam, String scope, String name, Variable variable) {
        super.setVariable(actionParam, scope, name, variable);
        this.storeVariable = variable;
    }

    public Variable getStoreVariable() {
        return storeVariable;
    }

    private boolean isNumber(String text) {
        return XUtils.isNumber(text);
    }
}
