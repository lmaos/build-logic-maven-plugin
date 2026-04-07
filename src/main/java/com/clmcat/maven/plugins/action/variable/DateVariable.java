package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;

import java.io.File;
import java.util.Date;

public class DateVariable extends Variable.AbstractVariable<Date> {

    private String format;
    private String timeZone;

    public DateVariable(Date value, String format, String timeZone) {
        super(value);
        this.format = format;
        this.timeZone = timeZone;
    }

    @Override
    public boolean isExist() {
        return getValue() != null;
    }

    @Override
    public String getStringValue() {
        return XUtils.formatDate(getValue(), format, timeZone);
    }
}
