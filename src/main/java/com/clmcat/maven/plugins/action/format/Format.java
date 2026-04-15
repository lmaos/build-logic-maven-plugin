package com.clmcat.maven.plugins.action.format;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.FunctionVariables;
import com.clmcat.maven.plugins.action.variable.VariableFactory;
import com.clmcat.maven.plugins.action.variable.map.VarOnlyReadMap;
import com.clmcat.maven.plugins.calculator.DefaultExpressionFormat;
import com.clmcat.maven.plugins.calculator.ExpressionFormat;
import com.clmcat.maven.plugins.calculator.IterativeExpressionCalculator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Format {
    public static ExpressionFormat  formatter = new DefaultExpressionFormat(new IterativeExpressionCalculator());

    public static String formatString(String text, FunctionVariables param, String around) {

        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        around = around == null ? "${?}"  : around;

        VarOnlyReadMap varOnlyReadMap = new VarOnlyReadMap(param);
        return formatter.format(text, around, varOnlyReadMap);

//        String[] split = around.split("\\?");
//
//        List<Node> nodes = new ArrayList<Node>();
//        String left = split[0];
//        String right = split.length < 2 ? "" : split[1];
//        int cursor = 0;
//        while (cursor < text.length()) {
//            int leftIndex = text.indexOf(left, cursor);
//            int rightIndex = leftIndex != -1 ? text.indexOf(right, leftIndex + left.length()) : -1;
//
//            if (leftIndex == -1 || rightIndex == -1) {
//                nodes.add(new StaticNode(text.substring(cursor, text.length())));
//                cursor = text.length();
//            } else {
//                nodes.add(new StaticNode(text.substring(cursor, leftIndex)));
//
//                String name = text.substring(leftIndex + left.length(), rightIndex);
//                String _text = text.substring(leftIndex, rightIndex + right.length());
//                nodes.add(new VariableNode(name, _text));
//                cursor = rightIndex  + right.length();
//            }
//        }
//        StringBuffer sb = new StringBuffer();
//        for (Node node : nodes) {
//            String format = node.format(param);
//            sb.append(format);
//        }
//        return sb.toString();
    }


//    public static interface Node {
//        String format(FunctionVariables param);
//    }
//
//
//    public static class StaticNode implements Node {
//        private String text;
//
//        public StaticNode(String text) {
//            this.text = text;
//        }
//
//        public String format(FunctionVariables param) {
//            return text;
//        }
//    }
//
//    public static class VariableNode implements Node {
//        private String name;
//        private String text;
//
//        public VariableNode(String name, String text) {
//            this.name = name;
//            this.text = text;
//        }
//
//        public String format(FunctionVariables param) {
//            String _name = this.name;
//            String _callFunctions[] = {};
//            int index = _name.indexOf("(");
//            if (index != -1) {
//
//                int start = _name.lastIndexOf(".", index);
//                if (start != -1) {
//                    _callFunctions = _name.substring(start + 1).split("\\.");
//                    _name = _name.substring(0, start);
//                }
//            }
//
//            Variable variable = param.getVariable(_name);
//            if (Variable.isNULL(variable)) {
//                return text;
//            }
//
//            if (_callFunctions == null || _callFunctions.length == 0) {
//                return variable.getStringValue();
//            }
//
//            try {
//                Object currValue = variable.getValue();
//                for (String function : _callFunctions) {
//                    if (currValue == null) {
//                        break;
//                    }
//                    String functionName = function;
//                    int paramStart = function.indexOf("(");
//                    int paramEnd = function.indexOf(")");
//                    String paramNamesArrs = null;
//                    if (paramStart != -1 && paramEnd != -1) {
//                        functionName = function.substring(0, paramStart);
//                        paramNamesArrs = function.substring(paramStart + 1, paramEnd).trim();
//                    }
//                    if (paramNamesArrs != null && paramNamesArrs.length() > 0) {
//                        String paramNames[] = XUtils.splitArguments(paramNamesArrs);
//                        Class[] paramTypes = new Class[paramNames.length];
//                        Object[] paramValues = new Object[paramNames.length];
//                        // java.lang.String name
//                        for (int i = 0; i < paramNames.length; i++) {
//                            int varIndex = paramNames[i].indexOf(" ");
//                            if (varIndex == -1) {
//                                paramValues[i] = null;
//                            } else {
//                                String paramClassName = paramNames[i].substring(0, varIndex).trim();
//                                String paramName = paramNames[i].substring(varIndex + 1).trim();
//                                Variable<?> paramVariable = param.getVariable(paramName, VariableFactory.newVariable(paramClassName, XUtils.unquote(paramName)));
//                                paramValues[i] = paramVariable.getValue();
//                                paramTypes[i] = XUtils.toSimpleClass(paramClassName);
//                            }
//                        }
//                        currValue = findMethod(functionName, currValue.getClass(), paramTypes).invoke(currValue, paramValues);
//                    } else {
//                        currValue = findMethod(functionName, currValue.getClass()).invoke(currValue);
//                    }
//                }
//                return String.valueOf(currValue);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("format variable error: " + this.name + ", text: " + text, e);
//            }
//        }
//    }
//
//    public static Method findMethod(String methodName, Class<?> beanType, Class<?> ... asClass) throws  NoSuchMethodException {
//        List<Method> candidates = new ArrayList<>();
//        for (Method method : beanType.getMethods()) {
//            if (method.getName().equals(methodName) && method.getParameterTypes().length == asClass.length) {
//                candidates.add(method);
//            }
//        }
//        Class<?> current = beanType;
//        while (current != Object.class) {
//            for (Method method : current.getDeclaredMethods()) {
//                if (method.getName().equals(methodName) && method.getParameterTypes().length == asClass.length) {
//                    candidates.add(method);
//                }
//            }
//            current = current.getSuperclass();
//        }
//
//        Method bestMatch = null;
//        int bestScore = Integer.MAX_VALUE;
//        for (Method candidate : candidates) {
//            int score = score(candidate.getParameterTypes(), asClass);
//            if (score >= 0 && score < bestScore) {
//                bestScore = score;
//                bestMatch = candidate;
//            }
//        }
//        if (bestMatch == null) {
//            throw new NoSuchMethodException("methodName:" + methodName + " not found; beanType:" + beanType + ", asClass:" + Arrays.toString(asClass));
//        }
//        bestMatch.setAccessible(true);
//        return bestMatch;
//    }
//
//    private static int score(Class<?>[] methodParameterTypes, Class<?>[] argumentTypes) {
//        int totalScore = 0;
//        for (int i = 0; i < methodParameterTypes.length; i++) {
//            Class<?> methodType = wrap(methodParameterTypes[i]);
//            Class<?> argumentType = argumentTypes[i] == null ? null : wrap(argumentTypes[i]);
//            if (argumentType == null) {
//                if (methodParameterTypes[i].isPrimitive()) {
//                    return -1;
//                }
//                totalScore += 10;
//                continue;
//            }
//            if (methodType == argumentType) {
//                continue;
//            }
//            if (methodType.isAssignableFrom(argumentType)) {
//                totalScore += 1;
//                continue;
//            }
//            return -1;
//        }
//        return totalScore;
//    }
//
//    private static Class<?> wrap(Class<?> type) {
//        if (type == null || !type.isPrimitive()) {
//            return type;
//        }
//        if (type == boolean.class) {
//            return Boolean.class;
//        }
//        if (type == byte.class) {
//            return Byte.class;
//        }
//        if (type == short.class) {
//            return Short.class;
//        }
//        if (type == int.class) {
//            return Integer.class;
//        }
//        if (type == long.class) {
//            return Long.class;
//        }
//        if (type == float.class) {
//            return Float.class;
//        }
//        if (type == double.class) {
//            return Double.class;
//        }
//        if (type == char.class) {
//            return Character.class;
//        }
//        return type;
//    }
}
