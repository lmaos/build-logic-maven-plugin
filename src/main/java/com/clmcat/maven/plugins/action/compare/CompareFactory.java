package com.clmcat.maven.plugins.action.compare;

import org.apache.maven.plugin.MojoExecutionException;

import javax.management.monitor.MonitorSettingException;

public class CompareFactory {
    public static Compare newInstance(String classname) {
        try {
            Class<?> compareType = Class.forName(classname);
            if (!Compare.class.isAssignableFrom(compareType)) {
                throw new IllegalArgumentException("Class is not a Compare implementation: " + classname);
            }
            return (Compare) compareType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create Compare implementation: " + classname, e);
        }
    }
}
