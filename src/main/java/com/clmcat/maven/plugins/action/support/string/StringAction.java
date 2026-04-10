package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import com.clmcat.maven.plugins.action.variable.ListVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import com.clmcat.maven.plugins.action.variable.number.IntVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.*;

/**
 * String operations.
 * <pre>
 *     {@code
 *     <!-- assign to 'to'; if not set, overwrites 'name' -->
 *     <str name="name" to="to" />
 *     <str.set name="name" value="hello" />
 *     <!-- substring of 'name', start=1, end=3, assign to to="aaa" -->
 *     <str.substr name="name" to="aaa" params="1,3"/>
 *     <!-- substring from position 1 to end -->
 *     <str.substr name="name" params="1"/>
 *     <!-- split string into a list -->
 *     <str.split name="name" to="" />
 *     <!-- random string; name can be a String variable or List variable; params="min,max,splitChar", at least one param required -->
 *     <str.random name=""  to="" params="10" />
 *     <str.trim name=""  />
 *     <str.trim name="" to="" />
 *     <str.toUpperCase name="" to="" />
 *     <str.toLowerCase name="" to="" />
 *     <str.replace name="" to="" params="aaa,ccc"/>
 *     <str.length name="str" to="strLen" />
 *     <str.len name="str" to="strLen" />
 *     <str.json name="obj" to="jsonStr" />
 *     <str.charAt name="str" to="strAt" params="0" />
 *
 *     }
 * </pre>
 * @author zxy
 */
public class StringAction extends Action.AbstractAction {

    private String name;
    private String to;
    private String params;
    private String ref;

    public String getName() {
        return name;
    }

    public String getParams() {
        return FunctionVariablesReference.format(params);
    }

    /**
     * 
     * @return string variable reference
     */
    public String getRef() {
        return ref;
    }


    public  static  interface StringMethod {
        // parse parameters
        default String[] paserParams(String _params){
            String[] params = null;
            if (XUtils.isNotEmpty(_params)) {
                params = _params.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(?=(?:[^']*'[^']*')*[^']*$)");
                for (int i = 0; i < params.length; i++) {
                    params[i] = XUtils.unquote(params[i].trim());
                }
            }
            return params;
        }
        Variable handler(StringAction stringAction,  String[] params, Variable variable) throws Exception;
    }


    private static Map<String, StringMethod> methods = new HashMap<>();
    static {
        StringMethod substring = new SubStringMethod();
        methods.put("substr", substring);
        methods.put("substring", substring);
        methods.put("charat", new CharAtStringMethod());
        methods.put("concat", new ConcatStringMethod());
        methods.put("length", new LengthStringMethod());
        methods.put("len", new LengthStringMethod());
        methods.put("replace", new ReplaceStringMethod());
        methods.put("trim", new TrimStringMethod());
        methods.put("toLowerCase", new ToLowerCaseStringMethod());
        methods.put("toUpperCase", new ToUpperCaseStringMethod());
        methods.put("set", new SetStringMethod());
        methods.put("split", new SplitStringMethod());
        methods.put("json", new ToJsonStringMethod());
        methods.put("tojson", new ToJsonStringMethod());
        methods.put("random", new RandomStringMethod());
        methods.put("append", new AppendStringMethod());



    }





    @Override
    protected void callExecute(ActionParam actionParam, Action action) throws Exception {



        if (!XUtils.isVariableName(name)) {
            throw new MojoExecutionException("any variable name must only contain $, 0-9, a-z, A-Z,_ (no dots allowed): <" + getTag() + " name=\"" + name + "\" />");
        }

        if (to != null) {
            if (!XUtils.isVariableName(to)) {
                throw new MojoExecutionException("any variable name must only contain $, 0-9, a-z, A-Z,_ (no dots allowed): <" + getTag() + " to=\"" + to + "\" />");
            }
        }


        String tagMethod = getTagMethod();
        if (XUtils.isEmpty(tagMethod)) {
            tagMethod = "set";
        }
        String _params = getParams();
        String methodName = tagMethod;





        Variable variable = getVariable(name);

        String value = getValue();
        if (XUtils.isNotEmpty(value)) {
            variable = StringVariable.of(value);
        }
        if (Variable.isNULL(variable)) {
            throw new Exception("variable is null,<" + getTag() + " name = " + name + ">");
        }


        ///  Simple initial implementation; structure to be refined later.
        StringMethod stringMethod = methods.get(methodName.toLowerCase());
        if (stringMethod == null) {
            throw new Exception("operation method not found,<" + getTag() + " name = " + name + ">");
        }
        String[] params = stringMethod.paserParams(_params);
        Variable resultVariable = stringMethod.handler(this, params, variable);

        if (XUtils.isNotEmpty(to)) {
            setVariable(actionParam, null, to, resultVariable);
        } else {
            setVariable(actionParam, null, name, resultVariable);
        }
    }
}
