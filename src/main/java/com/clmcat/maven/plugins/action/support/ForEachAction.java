package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.CodeBlockAction;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.*;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ForEachAction extends CodeBlockAction.AbstractCodeBlockAction {
    // 变量名
    private String collection;
    // item 变量名
    private String item = "item";
    @Override
    public void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
        String collectionName = actionParam.format(collection);
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new MojoExecutionException("<foreach> collection attribute is required");
        }
        Variable variable = getFunctionVariables().getVariable(collectionName);

        FunctionVariable thisFunctionVariable = getThisFunctionVariable();

        if (Variable.isExist(variable)) {
            if (variable.getValue() instanceof Collection) {
                Collection<?> colls = (Collection<?>) variable.getValue();
                int index = 0;
                for (Object coll : colls) {
                    Variable itemVariable = VariableFactory.newVariable(coll);
                    thisFunctionVariable.setVariable(this.item, itemVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            } else if (variable instanceof MapVariable) {
                Map<?, ?> map = ((MapVariable)variable).getValue();
                int index = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Variable itemVariable = VariableFactory.newVariable(entry.getKey());
                    Variable valueVariable = VariableFactory.newVariable(entry.getValue());
                    thisFunctionVariable.setVariable(this.item+".key", itemVariable);
                    thisFunctionVariable.setVariable(this.item+".value", valueVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            } else if (variable.getValue().getClass().isArray()) {
                Object array = variable.getValue();
                int length = Array.getLength(array);
                for (int i = 0; i < length; i++) {
                    Object b = Array.get(array, i);
                    Variable itemVariable = VariableFactory.newVariable(b);
                    thisFunctionVariable.setVariable(this.item, itemVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(i));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            } else if (variable instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable)variable;
                File file = fileVariable.getValue();
                if (file.isFile()) {
                    thisFunctionVariable.setVariable(this.item, fileVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(0));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                } else {
                    // 是目录的情况
                    LinkedList<File> dirs = new LinkedList<>();
                    dirs.add(file);
                    int index = 0;
                    while (!dirs.isEmpty()) {
                        File dir = dirs.removeFirst();
                        thisFunctionVariable.setVariable(this.item, FileVariable.of(dir));
                        thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                        super.callCodeBlockExecute(actionParam, parentAction, actions);
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File subFile : files) {
                                if (subFile.isFile()) {
                                    thisFunctionVariable.setVariable(this.item, FileVariable.of(subFile));
                                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                                } else {
                                    dirs.add(subFile);
                                }
                            }
                        }
                    }
                }
            } else {
                throw new MojoExecutionException("<foreach collection=\"" + collectionName
                        + "\"> only supports Collection, Map, array, File or numeric range");
            }
        } else {
            // 1..2
            int i = collectionName.indexOf("..");
            if (i != -1) {
                int index = Integer.parseInt(collectionName.substring(0, i).trim());
                int end = Integer.parseInt(collectionName.substring(i+2).trim());
                for (int j = index; j <= end; j++) {
                    Variable itemVariable = VariableFactory.newVariable(j);
                    thisFunctionVariable.setVariable(this.item, itemVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(j - index));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            } else {
                throw new MojoExecutionException("Unknown foreach collection: " + collectionName);
            }
        }
    }
}
