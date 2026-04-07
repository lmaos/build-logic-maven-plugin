package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.StringVariable;

import java.util.Arrays;

public class SubStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String tag = stringAction.getTag();
        String name = stringAction.getName();
        if (params == null || params.length == 0) {
            throw new Exception("substring params is null");
        } else if (params.length == 1) {
            String stringValue = variable.getStringValue();
            try {
                int i = Integer.parseInt(params[0]);
                if (i < 0 || i > stringValue.length()) {
                    throw new Exception("substring params is out of range,<" + tag + " name = \"" + name + "\"; startIndex = " + i);
                }
                return StringVariable.of(stringValue.substring(i));
            } catch (NumberFormatException e) {
                throw new Exception("substring params is not number,<" + tag + " name = \"" + name + "\"");
            }
        } else if (params.length == 2) {
            String stringValue = variable.getStringValue();
            try {
                int start = Integer.parseInt(params[0]);
                int end = Integer.parseInt(params[1]);
                if (start < 0 || start > stringValue.length() || end < 0 || end > stringValue.length()) {
                    throw new Exception("substring params is out of range,<" + tag + " name = \"" + name + "\"; startIndex = " + start + ", endIndex = " + end);
                }
                return  StringVariable.of(stringValue.substring(start, end));
            } catch (NumberFormatException e) {
                throw new Exception("substring params is not number,<" + tag + " name = \"" + name + "\". params = " + Arrays.toString(params));
            }
        } else {
            throw new Exception("substring params is not valid,<" + tag + " name = \"" + name + "\". params = " + java.util.Arrays.toString(params));
        }
    }
}
