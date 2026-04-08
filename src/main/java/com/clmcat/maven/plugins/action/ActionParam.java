package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.compare.Compare;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import com.clmcat.maven.plugins.action.format.Format;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariableItem;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

public class ActionParam {



    private Mojo mojo;
    private ActionExecute actionExecute;
    private MavenProject project;
    private Compare compare;
    private FunctionVariable functionVariable ;

    public ActionParam(Mojo mojo, ActionExecute actionExecute, MavenProject project, Compare compare, FunctionVariable functionVariable) {
        this.mojo = mojo;
        this.actionExecute = actionExecute;
        this.project = project;
        this.compare = compare;
        this.functionVariable = functionVariable;
    }


    public ActionExecute getActionExecute() {
        return actionExecute;
    }

    public ActionFactory getActionFactory() {
        return actionExecute.getActionFactory();
    }

    public Mojo getMojo() {
        return mojo;
    }


    public void info(String info) {
        mojo.getLog().info(format(info));
    }
    public void debug(String info) {
        mojo.getLog().debug(format(info));
    }

    public void warn(String warn) {
        mojo.getLog().warn(format(warn));
    }
    public void error(String error) {
        mojo.getLog().error(format(error));
    }
    public void error(String error, Exception e) {
        mojo.getLog().error(format(error), e);
    }


    public void setVariable(String name, Variable value) {
        functionVariable.setVariable(name, value);
    }

    public Variable getVariable(String name) {
        return functionVariable.getVariable(name);
    }

    public Variable getVariable(String name, Variable defaultValue) {
        return functionVariable.getVariable(name, defaultValue);
    }


    public boolean test(String test) {
        FunctionVariables functionVariables = FunctionVariablesReference.getFunctionVariables();
        boolean result = compare.compare(test, functionVariables);
        return result;
    }

    public String format(String text) {
        return FunctionVariablesReference.format(text);
    }

    public FunctionVariable getFunctionVariable() {
        return functionVariable;
    }
}
