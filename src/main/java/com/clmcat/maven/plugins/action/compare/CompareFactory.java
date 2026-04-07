package com.clmcat.maven.plugins.action.compare;

import org.apache.maven.plugin.MojoExecutionException;

import javax.management.monitor.MonitorSettingException;

public class CompareFactory {
    public static Compare newInstance(String classname) {
        try {
            return (Compare) Class.forName(classname).newInstance();
        } catch (Exception e) {
            throw  new MonitorSettingException("class " + classname + " not found");
        }
    }
}
