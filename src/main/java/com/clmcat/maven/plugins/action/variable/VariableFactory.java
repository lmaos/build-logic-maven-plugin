package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.number.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VariableFactory {

    public  static Variable newVariable(Object value){
        if (value == null){
            return null;
        }
        if (value instanceof Variable){
            return (Variable)value;
        }
        if (value instanceof List){
            return new ListVariable((List<?>)value);
        }
        if (value instanceof String){
            return new StringVariable((String)value);
        }
        if (value instanceof Map){
            return new MapVariable((Map<?, ?>)value);
        }
        if (value instanceof Boolean){
            return new BooleanVariable((Boolean)value);
        }
        if (value instanceof Number){
            return new NumberVariable(new java.math.BigDecimal(value.toString()));
        }
        if (value instanceof byte[]){
            return new BytesVariable((byte[])value);
        }
        if (value instanceof File){
            return new FileVariable((File)value);
        }
        if (value instanceof Date){
            return new DateVariable((Date)value, "yyyy-MM-dd HH:mm:ss", "GMT+8");
        }
        return new DefaultVariable(value);
    }


    public static FunctionVariable newSingleFunctionVariable(Action action){
        return new FunctionVariableItem(action);
    }

    public static Variable newVariable(String type, String value){

        if (XUtils.isEmpty(type)){
            if (XUtils.isNumber(value)){
                if (value.indexOf(".") != -1){
                    return NumberVariable.of(value);
                } else {
                    return IntVariable.of(value);
                }
            } else {
                return StringVariable.of(value);
            }
        } else {
            int index = type.indexOf("?");
            String typeName = index == -1 ? type : type.substring(0, index);
            if (typeName.equalsIgnoreCase("number")
                    || typeName.equalsIgnoreCase("BigDecimal")){
                return NumberVariable.of(value);
            } else if (typeName.equalsIgnoreCase("string")){
                return StringVariable.of(value);
            } else if (typeName.equalsIgnoreCase("date")){
                // type="date?fyyyy-MM-dd HH:mm:ss?zGMT+8"
                if (index == -1){
                    return new DateVariable(new Date(Long.parseLong(value.trim())), "yyyy-MM-dd HH:mm:ss", "GMT+8");
                } else {
                    String[] split = type.substring(index + 1).split("\\?");
                    String format = "yyyy-MM-dd HH:mm:ss";
                    String timeZone = "GMT+8";
                    for (String string : split) {
                        if (string.contains("f")){
                            format =  string.substring(1).trim();
                        } else if (string.contains("z")){
                            timeZone = string.substring(1).trim();
                        }
                    }
                    return new DateVariable(XUtils.parseDate(value, format, timeZone), format, timeZone);
                }
            } else if (typeName.equalsIgnoreCase("file")) {
                return FileVariable.of(new File(value));
            } else {
                try {
                    Class<?> clazz = XUtils.toSimpleClass(type);
                    if (clazz == int.class || clazz == Integer.class){
                        return IntVariable.of(value);
                    }  else if (clazz == double.class || clazz == Double.class){
                        return DoubleVariable.of(value);
                    }  else if (clazz == long.class || clazz == Long.class){
                        return LongVariable.of(value);
                    } else if (clazz == float.class || clazz == Float.class){
                        return FloatVariable.of(value);
                    } else if (clazz == byte.class || clazz == Byte.class){
                        return ByteVariable.of(value);
                    } else if (clazz == short.class || clazz == Short.class){
                        return ShortVariable.of(value);
                    } else if (clazz == boolean.class || clazz == Boolean.class){
                        return BooleanVariable.of(value);
                    } else if (clazz == BigDecimal.class) {
                        return NumberVariable.of(new BigDecimal(value));
                    } else if (clazz == File.class) {
                        return FileVariable.of(new File(value));
                    } else  {
                        // unsupported string-to-type conversion
                        throw new IllegalArgumentException("not support type convert: " + type+", value: "+value);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }


}
