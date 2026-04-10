package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.ListVariable;

import java.util.Arrays;
import java.util.List;
/**
 * String split method implementation.
 * <p>
 * Splits a string into a list of strings using a specified delimiter.
 * </p>
 */
public class SplitStringMethod implements StringAction.StringMethod {

    @Override
    public String[] paserParams(String _params) {
        if (XUtils.isEmpty(_params)) {
            return null;
        }
        return new String[]{_params};
    }
    /**
     * Handle a string split operation.
     *
     * @param stringAction the StringAction instance containing operation metadata
     * @param params method parameter array; the first element is the delimiter
     * @param variable the variable to process
     * @return a ListVariable containing the split string elements
     * @throws Exception if an error occurs during processing
     */
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String stringValue = variable.getStringValue();
        if (params == null || params.length == 0) {
            return ListVariable.of(Arrays.asList(stringValue));
        }
        String name = stringAction.getName();
        String tag = stringAction.getTag();
        String splitParam = params[0];
        String[] array = stringValue.split(splitParam);
        List<String> list = Arrays.asList(array);
        return ListVariable.of(list);
    }
}
