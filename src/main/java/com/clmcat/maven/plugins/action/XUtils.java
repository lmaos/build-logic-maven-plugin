package com.clmcat.maven.plugins.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class XUtils {

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

}
