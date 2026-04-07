package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.DateVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateAction extends VariableAction {

    private String format = "yyyy-MM-dd HH:mm:ss";
    private String timeZone = "GMT+8";



    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

        if (XUtils.isEmpty(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        if (XUtils.isEmpty(timeZone)) {
            timeZone = "GMT+8";
        }

        TimeZone tz = TimeZone.getTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(tz);


        if (getName() == null) {
            throw new MojoExecutionException("<date name=null> name is null;  <date format=\""+format+"\" timeZone=\""+timeZone+"\">"+simpleDateFormat.format(new Date())+"</date>");
        }

        String dateValue = getValue();
        Date date = null;
        if (XUtils.isEmpty(dateValue)) {
            date = new Date();
        } else {
            date = simpleDateFormat.parse(dateValue);
        }
        setVariable(actionParam, new DateVariable(date, format, timeZone));
    }



}
