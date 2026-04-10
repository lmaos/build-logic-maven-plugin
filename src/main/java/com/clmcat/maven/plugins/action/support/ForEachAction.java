package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.CodeBlockAction;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.*;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ForEachAction extends CodeBlockAction.AbstractCodeBlockAction {
    // collection name
    private String collection;
    // item variable name
    private String item = "item";
    @Override
    public void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
        Variable variable = getFunctionVariables().getVariable(collection);

        FunctionVariable thisFunctionVariable = getThisFunctionVariable();

        if (variable.isExist()) {
            if (variable.getValue() instanceof Collection) {
                Collection<?> colls = (Collection<?>) variable.getValue();
                int index = 0;
                for (Object coll : colls) {
                    Variable itemVariable = VariableFactory.newVariable(coll);
                    getFunctionVariables().getFirst().setVariable(this.item, itemVariable);
                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            } else if (variable instanceof MapVariable) {
                Map<?, ?> map = ((MapVariable)variable).getValue();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Variable itemVariable = VariableFactory.newVariable(entry.getKey());
                    Variable valueVariable = VariableFactory.newVariable(entry.getValue());
                    thisFunctionVariable.setVariable(this.item+".key", itemVariable);
                    thisFunctionVariable.setVariable(this.item+".value", valueVariable);
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
                    getFunctionVariables().getFirst().setVariable(this.item, fileVariable);
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                } else {
                    // handle the case where the FileVariable is a directory
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
                                    getFunctionVariables().getFirst().setVariable(this.item, FileVariable.of(subFile));
                                    thisFunctionVariable.setVariable(this.item+".index", NumberVariable.of(index++));
                                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                                } else {
                                    dirs.add(subFile);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 1..2
            this.collection = actionParam.format(collection);
            int i = collection.indexOf("..");
            if (i != -1) {
                int index = Integer.parseInt(collection.substring(0, i));
                int end = Integer.parseInt(collection.substring(i+2));
                for (int j = index; j <= end; j++) {
                    Variable itemVariable = VariableFactory.newVariable(j);
                    getFunctionVariables().getFirst().setVariable(this.item, itemVariable);
                    super.callCodeBlockExecute(actionParam, parentAction, actions);
                }
            }
        }
    }
}
