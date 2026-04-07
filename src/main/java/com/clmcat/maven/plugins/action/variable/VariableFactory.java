package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;

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
                return NumberVariable.of(value);
            } else {
                return StringVariable.of(value);
            }
        } else {
            if (type.startsWith("number")){
                return NumberVariable.of(value);
            } else if (type.startsWith("string")){
                return StringVariable.of(value);
            } else if (type.startsWith("date")){
                // type="date?fyyyy-MM-dd HH:mm:ss?zGMT+8"
                int index = type.indexOf("?");
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
            } else if (type.startsWith("file")){
                return FileVariable.of(new File(value));
            } else {
                return StringVariable.of(value);
            }
        }

    }


}
