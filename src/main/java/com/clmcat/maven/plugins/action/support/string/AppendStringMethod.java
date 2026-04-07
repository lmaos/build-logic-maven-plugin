package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.StringVariable;

public class AppendStringMethod implements StringAction.StringMethod {


    @Override
    public String[] paserParams(String _params) {
        if (XUtils.isEmpty(_params)) {
            return null;
        }
        return new String[]{_params};
    }

    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String stringValue = variable.getStringValue();
        if(params == null || params.length == 0){
            String ref = stringAction.getRef();
            if(XUtils.isEmpty(ref)){
                return StringVariable.of(stringValue);
            }
            Variable refVariable = stringAction.getVariable(ref);
            if (Variable.isExist(refVariable)) {
                return StringVariable.of(stringValue + refVariable.getStringValue());
            } else  {
                return StringVariable.of(stringValue);
            }
        } else {
            return StringVariable.of(stringValue + params[0]);
        }
    }
}
