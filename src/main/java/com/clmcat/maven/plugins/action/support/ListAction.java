package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import com.clmcat.maven.plugins.action.variable.ListVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
* 列表操作.
* 使用方法:
* <pre>
* {@code
* <!-- 创建列表项 -->
* <list name="appList">
*     <item>item1</item>
*     <item>item2</item>
* </list>
 *
 * <!-- 添加列表项, 不存在变量则创建 -->
 * <list.add name="appList">
 *     <item>item1</item>
 *     <item>item2</item>
 * </list>
*}</pre>
*@aauthor zxy
*  */
public class ListAction extends VariableAction {

    private static final Set<String> METHODS = XUtils.toSet("set", "add", "remove");
    private Integer index;
    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String method = getTagMethod();
        String _method = XUtils.isEmpty(method) ? "set" : method;

        if (!METHODS.contains(_method)) {
            throw new MojoExecutionException("<list method=\""+_method+"\" /> only support set or add");
        }

        ActionFactory actionFactory = actionParam.getActionExecute().getActionFactory().create();
        actionFactory.addActionType("item", VariableAction.class);
        actionFactory.addActionType("var", VariableAction.class);
        actionFactory.addActionType("file", FileAction.class);
        actionFactory.addActionType("date", DateAction.class);
        actionFactory.addActionType("read", ReadAction.class);

        List<Action> actions = parseChildren(actionFactory);
        List<Variable> variables = new ArrayList<Variable>();
        FunctionVariablesReference.execute(this, functionVariable -> {
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if(action instanceof VariableAction){
                    VariableAction variableAction = (VariableAction) action;
                    variableAction.setScope("this");
                    variableAction.setName(getName()+"_"+i);
                    variableAction.execute(actionParam, this);
                    Variable storeVariable = variableAction.getStoreVariable();
                    variables.add(storeVariable);
                } else {
                    throw new MojoExecutionException("ListAction only support VariableAction");
                }
            }
        });

        if (_method.equals("set")) {
            setVariable(actionParam, new ListVariable(variables));
        } else if (_method.equals("add")) {
            Variable variable = getVariable(getName());
            if (Variable.isNULL(variable)) {
                setVariable(actionParam, new ListVariable(variables));
            } else if (variable instanceof ListVariable) {
                ((ListVariable)variable).addAll(variables);
            } else {
                throw new MojoExecutionException("<list.add name=\""+getName()+"\" /> only support ListVariable");
            }
        }  else if (_method.equals("remove")) {
            Variable variable = getVariable(getName());
            if (Variable.isNULL(variable)) {
                throw  new MojoExecutionException("<list.remove>  list variable is null");
            }
            if (variable instanceof ListVariable) {
                int size = ((ListVariable) variable).size();
                if (index == null) {
                    throw new MojoExecutionException("<list.remove index=\""+index+"\">  index is null");
                }
                if (index < 0 || index >= size) {
                    throw new MojoExecutionException("<list.remove index=\""+index+"\">  index is out of range: size=" + size);
                }

                ((ListVariable)variable).remove(index);
            }

        }


    }
}
