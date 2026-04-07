package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.apache.maven.plugin.MojoExecutionException;

public class ReplaceStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String tag = stringAction.getTag();
        String name = stringAction.getName();
        if (params.length != 2) {
           throw  new MojoExecutionException("replace method must have 2 parameters, <" + tag + " name=\"" + name + "\" />");
        }
        String param0 = params[0];
        String param1 = params[1];

        return StringVariable.of(variable.getStringValue().replace(param0, param1));
    }
}
