package com.clmcat.maven.plugins.action.format;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.StringVariable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Format {

    public static String formatString(String text, FunctionVariables param, String around) {

        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        around = around == null ? "${?}"  : around;
        String[] split = around.split("\\?");

        List<Node> nodes = new ArrayList<Node>();
        String left = split[0];
        String right = split.length < 2 ? "" : split[1];
        int cursor = 0;
        while (cursor < text.length()) {
            int leftIndex = text.indexOf(left, cursor);
            int rightIndex = leftIndex != -1 ? text.indexOf(right, leftIndex + left.length()) : -1;

            if (leftIndex == -1 || rightIndex == -1) {
                nodes.add(new StaticNode(text.substring(cursor, text.length())));
                cursor = text.length();
            } else {
                nodes.add(new StaticNode(text.substring(cursor, leftIndex)));

                String name = text.substring(leftIndex + left.length(), rightIndex);
                String _text = text.substring(leftIndex, rightIndex + right.length());
                nodes.add(new VariableNode(name, _text));
                cursor = rightIndex  + right.length();
            }
        }
        StringBuffer sb = new StringBuffer();
        for (Node node : nodes) {
            String format = node.format(param);
            sb.append(format);
        }
        return sb.toString();
    }


    public static interface Node {
        String format(FunctionVariables param);
    }


    public static class StaticNode implements Node {
        private String text;

        public StaticNode(String text) {
            this.text = text;
        }

        public String format(FunctionVariables param) {
            return text;
        }
    }

    public static class VariableNode implements Node {
        private String name;
        private String text;

        public VariableNode(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public String format(FunctionVariables param) {
            String _name = this.name;
            String _callFunctions[] = {};
            int index = _name.indexOf("(");
            if (index != -1) {

                int start = _name.lastIndexOf(".", index);
                if (start != -1) {
                    _callFunctions = _name.substring(start + 1).split("\\.");
                    _name = _name.substring(0, start);
                }
            }

            Variable variable = param.getVariable(_name);
            if (Variable.isNULL(variable)) {
                return text;
            }

            if (_callFunctions == null || _callFunctions.length == 0) {
                return variable.getStringValue();
            }

            try {
                Object currValue = variable.getValue();
                for (String function : _callFunctions) {
                    if (currValue == null) {
                        break;
                    }
                    String functionName = function;
                    int paramStart = function.indexOf("(");
                    int paramEnd = function.indexOf(")");
                    String paramNamesArrs = null;
                    if (paramStart != -1 && paramEnd != -1) {
                        functionName = function.substring(0, paramStart);
                        paramNamesArrs = function.substring(paramStart + 1, paramEnd).trim();
                    }
                    if (paramNamesArrs != null && paramNamesArrs.length() > 0) {
                        String paramNames[] = paramNamesArrs.split(",");
                        Class[] paramTypes = new Class[paramNames.length];
                        Object[] paramValues = new Object[paramNames.length];
                        // java.lang.String name
                        for (int i = 0; i < paramNames.length; i++) {
                            int varIndex = paramNames[i].indexOf(" ");
                            if (varIndex == -1) {
                                paramValues[i] = null;
                            } else {
                                String paramClassName = paramNames[i].substring(0, varIndex).trim();
                                String paramName = paramNames[i].substring(varIndex + 1).trim();
                                Variable<?> paramVariable = param.getVariable(paramName);
                                paramValues[i] = paramVariable.getValue();
                                paramTypes[i] = Class.forName(paramClassName);
                            }
                        }
                        currValue = findMethod(functionName, currValue.getClass(), paramTypes).invoke(currValue, paramValues);
                    } else {
                        currValue = findMethod(functionName, currValue.getClass()).invoke(currValue);
                    }
                }
                return String.valueOf(currValue);
            } catch (Exception e) {
                throw new RuntimeException(" format variable error: " + this.name + ", text: " + text, e);
            }

        }
    }

//    public Field findField(String fieldName, Class<?> fieldType) throws NoSuchFieldException {
//        Class<?> aClass = fieldType;
//        while (aClass != Object.class) {
//            try {
//                Field declaredField = aClass.getDeclaredField(fieldName);
//                return declaredField;
//            } catch (NoSuchFieldException e) {
//                aClass =  aClass.getSuperclass();
//            }
//        }
//        throw new NoSuchFieldException("Field:" + fieldName + " not found");
//    }

    public static Method findMethod(String methodName, Class<?> beanType, Class<?> ... asClass) throws  NoSuchMethodException {
        Class<?> aClass = beanType;
        while (aClass != Object.class) {
            try {
                Method declaredField = aClass.getDeclaredMethod(methodName, asClass);
                declaredField.setAccessible(true);
                return declaredField;
            } catch (NoSuchMethodException e) {
                aClass =  aClass.getSuperclass();
            }
        }
        throw new NoSuchMethodException("methodName:" + methodName + " not found; beanType:" + beanType + ", asClass:" + Arrays.toString(asClass));
    }
}
