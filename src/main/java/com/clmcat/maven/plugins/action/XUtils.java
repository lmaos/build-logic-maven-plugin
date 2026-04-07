package com.clmcat.maven.plugins.action;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XUtils {

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }
    public static boolean isEmpty(String str){
        return str == null || str.trim().isEmpty();
    }

    public static String formatDate(long date, String format, String timeZone){
        return formatDate(new Date(date), format, timeZone);
    }
    public static String formatDate(Date date, String format, String timeZone){
        if (date == null) {
            return null;
        }
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(tz);
        return simpleDateFormat.format(date);
    }

    public static Date parseDate(String date, String format, String timeZone){
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(tz);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumber(String str){
        if (str == null || str.trim().isEmpty()){
            return false;
        }
        try {
            new java.math.BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String unquote(String str) {
        if (str.startsWith("\"") && str.endsWith("\"") || str.startsWith("'") && str.endsWith("'")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static <T> java.util.Set<T> toSet(T ... items){
        java.util.Set<T> set = new java.util.HashSet<>(items.length);
        for (T item : items) {
            set.add(item);
        }
        return set;
    }

    public static <T> java.util.List<T> toList(T ... items){
        java.util.List<T> list = new java.util.ArrayList<>(items.length);
        for (T item : items) {
            list.add(item);
        }
        return list;
    }

    private static Set<Class<?>> VALUE_TYPES = new HashSet<Class<?>>();
    static {
        VALUE_TYPES.add(String.class);
        VALUE_TYPES.add(Boolean.class);
        VALUE_TYPES.add(Character.class);
        VALUE_TYPES.add(Byte.class);
        VALUE_TYPES.add(Short.class);
        VALUE_TYPES.add(Integer.class);
        VALUE_TYPES.add(Long.class);
        VALUE_TYPES.add(Float.class);
        VALUE_TYPES.add(Double.class);
        VALUE_TYPES.add(Date.class);

    }

    /**
     * 将任意对象转换 JSON String 的简便方法，
     * @param object 任意非值对象
     * @param allString 值全部作为 String 使用。
     * @return JSON String
     */
    public static String toJsonString(Object object, boolean allString) {
        return toJsonStringBuilder(object, allString).toString();
    }

    public static String toJsonString(Object object) {
        return toJsonStringBuilder(object, false).toString();
    }

    public static StringBuilder toJsonStringBuilder(Object object, boolean allString) {

        if (object == null || object instanceof Class || object.getClass().isInterface()) {
            return null;
        }
        StringBuilder  sb = new StringBuilder();
        if (object instanceof Map) {
            Map<?,?> map = (Map<?, ?>) object;
            sb.append('{');
            for (Map.Entry<?,?> o : map.entrySet()) {
                String key = o.getKey().toString();
                Object value = o.getValue();
                StringBuilder valueString = toJsonStringBuilder(value, allString);
                if (valueString != null) {
                    sb.append('"').append(key).append('"').append(':').append(valueString);
                    sb.append(',');
                }
            }
            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append('}');
        } else if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            sb.append('[');
            for (Object o : collection) {
                StringBuilder valueJson = toJsonStringBuilder(o, allString);
                if (valueJson != null) {
                    sb.append(valueJson);
                    sb.append(',');
                }
            }
            sb.deleteCharAt(sb.length() - 1);

            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(']');
        } else if (object instanceof CharSequence) {
            sb.append('"').append(object.toString()).append('"');
        } else if (object instanceof Number || object instanceof Boolean) {
            if (allString || object instanceof BigDecimal || object instanceof BigInteger) {
                sb.append('"').append(object.toString()).append('"');
            }  else {
                sb.append(object.toString());
            }
        } else if (object instanceof Date) {
            if (allString) {
                sb.append('"').append(((Date)object).getTime()).append('"');
            }  else {
                sb.append(((Date)object).getTime());
            }
        } else if (VALUE_TYPES.contains(object.getClass())) {
            sb.append('"').append(object.toString()).append('"');
        } else {
            Class<?> aClass = object.getClass();
            // 读取所有get方法
            Method[] methods = aClass.getMethods();
            sb.append("{");
            for (Method m : methods) {
                if (m.getParameterCount() == 0 && m.getReturnType() != void.class) {
                    String name = m.getName();
                    String key = null;
                    if (name.startsWith("get") && name.length() > 3) {
                        key = name.substring(3, 4).toLowerCase() + name.substring(4);
                    }  else if (name.startsWith("is") && name.length() > 2) {
                        key = name.substring(2, 3).toLowerCase() + name.substring(3);
                    }
                    if (key != null) {
                        try {
                            Object value = m.invoke(object);
                            StringBuilder valueJson = toJsonStringBuilder(value, allString);
                            if (valueJson != null) {
                                sb.append('"').append(key).append('"').append(':').append(valueJson);
                                sb.append(',');
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append('}');
        }
        return sb;
    }

    public static String readFile(File file, String encoding) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int len = 0;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readFileToBytes(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int len = 0;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toBytes(String value, String encoding) {
        return value == null ? new byte[0] : value.getBytes(Charset.forName(encoding));
    }
    public static byte[] toUrlBytes(String value, String encoding) {
        value = toUrlString(value, encoding);
        return value == null ? new byte[0] : value.getBytes(Charset.forName(encoding));
    }

    public static String toUrlString(String value, String encoding) {
        if (value == null) return null;
        int i = value.indexOf("?");
        StringBuffer sb = new StringBuffer();
        if (i != -1) {
            sb.append(value.substring(0, i + 1));
            value = value.substring(i + 1);
        }
        if (value.indexOf('&') != -1) {
            String[] params = value.split("&");
            for (String param : params) {
                if (param.trim().length() == 0) continue;
                i = param.indexOf("=");
                if (i != -1) {
                    try {
                        sb.append(param.substring(0, i)).append("=").append(URLEncoder.encode(param.substring(i + 1), encoding)).append("&");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } else  {
            return value;
        }

    }
    public static Long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {

            return null;
        }
    }

    private static Map<String, Class<?>> paramTypes = new HashMap<>();
    static {
        paramTypes.put("String", String.class);
        paramTypes.put("string", String.class);
        paramTypes.put("long", long.class);
        paramTypes.put("Long", Long.class);
        paramTypes.put("int", int.class);
        paramTypes.put("Integer", Integer.class);
        paramTypes.put("short", short.class);
        paramTypes.put("Short", Short.class);
        paramTypes.put("double", double.class);
        paramTypes.put("Double", Double.class);
        paramTypes.put("float", float.class);
        paramTypes.put("Float", Float.class);
        paramTypes.put("Boolean", Boolean.class);
        paramTypes.put("boolean", boolean.class);
        paramTypes.put("char", char.class);
        paramTypes.put("Character", Character.class);
        paramTypes.put("BigDecimal", BigDecimal.class);
        paramTypes.put("BigInteger", BigInteger.class);

        paramTypes.put("Date", Date.class);
        paramTypes.put("byte[]", byte[].class);
        paramTypes.put("int[]", int[].class);
        paramTypes.put("long[]", long[].class);
        paramTypes.put("double[]", double[].class);
        paramTypes.put("float[]", float[].class);
        paramTypes.put("char[]", char[].class);
        paramTypes.put("boolean[]", boolean[].class);
        paramTypes.put("short[]", short[].class);
        paramTypes.put("Integer[]", Integer[].class);
        paramTypes.put("Long[]", Long[].class);
        paramTypes.put("Short[]", Short[].class);
        paramTypes.put("Double[]", Double[].class);
        paramTypes.put("Float[]", Float[].class);
        paramTypes.put("Character[]", Character[].class);
        paramTypes.put("Boolean[]", Boolean[].class);
        paramTypes.put("String[]", String[].class);
        paramTypes.put("StringBuilder", StringBuffer.class);
        paramTypes.put("List", List.class);
        paramTypes.put("Map", Map.class);
        paramTypes.put("Set", Set.class);
        paramTypes.put("ArrayList", ArrayList.class);
        paramTypes.put("LinkedList", LinkedList.class);
        paramTypes.put("HashSet", HashSet.class);
        paramTypes.put("HashMap", HashMap.class);
        paramTypes.put("Number", BigDecimal.class);
        paramTypes.put("File", File.class);



    }


    public static Class<?> toSimpleClass(String className) throws ClassNotFoundException {
        try {

            if (paramTypes.containsKey(className)) {
                return paramTypes.get(className);
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }


    public boolean isNumber(Class<?> clazz) {
        if ( clazz.isAssignableFrom(Number.class))  {
            return true;
        } else if (clazz == int.class || clazz == long.class || clazz == double.class || clazz == float.class || clazz == byte.class || clazz == short.class) {
            return true;
        }
        return false;
    }

    public  boolean isBoolean(Class<?> clazz) {
        if ( clazz ==  boolean.class || clazz == Boolean.class)  {
            return true;
        }
        return false;
    }
    /*
     * 校验变量名是否符合要求, 只能包含字母、数字、下划线和美元符号
     */
    public static boolean isVariableName(String name) {
        return name != null && name.matches("^[$0-9a-zA-Z_]+$");
    }
}
