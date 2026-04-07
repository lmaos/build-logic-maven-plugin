package com.clmcat.maven.plugins.action.factory;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.GroupAction;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.management.monitor.MonitorSettingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DefaultActionFactory implements ActionFactory {

    private Map<String, Class<?>> actionTypeMap = new HashMap<>();

    public DefaultActionFactory addActionType(String name, Class<?> actionType) {
        actionTypeMap.put(name.toLowerCase(), actionType);
        return this;
    }

    @Override
    public <T extends Action> T newInstance(PlexusConfiguration config) {
        Class<?> aClass = actionTypeMap.get(getTagName(config.getName()).toLowerCase());
        if (aClass == null) {
            return  null;
        }
        return newInstance(config, aClass);
    }

    @Override
    public ActionFactory copy() {
        DefaultActionFactory copy = new DefaultActionFactory();
        copy.actionTypeMap.putAll(this.actionTypeMap);
        return copy;
    }

    private String getTagName(String name) {
        if (name.contains(".")) {
            return name.substring(0, name.indexOf("."));
        }
        return name.trim();
    }

    @Override
    public ActionFactory create() {
        DefaultActionFactory create = new DefaultActionFactory();
        return create;
    }

    public <T extends Action> T newInstance(PlexusConfiguration config , Class<?> actionClass) {

        try {
            String name = config.getName();
            T t = (T)actionClass.newInstance();
            String value = config.getValue();
            t.setValue(value);
            t.setTag(name);
            if (name.contains(".")) {
                t.setTagMethod(name.substring(name.indexOf(".") + 1));
                name = name.substring(0, name.indexOf("."));
            }

            if (t instanceof PlexusConfigurationAware) {
                ((PlexusConfigurationAware) t).setPlexusConfiguration(config);
            }

            ///  组 Action
            if (t instanceof GroupAction) {
                PlexusConfiguration[] children = config.getChildren();
                if (children != null && children.length > 0) {
                    for (PlexusConfiguration childConfig : children) {
                        T t1 = (T) newInstance(childConfig);
                        if (t1 != null) {
                            ((GroupAction) t).addAction(t1);
                        }
                    }
                }
            }

            String[] attributeNames = config.getAttributeNames();
            if (attributeNames != null && attributeNames.length > 0) {
                for (String attributeName : attributeNames) {
                    try {
                        Field declaredField = findField(attributeName, actionClass);

                        int modifiers = declaredField.getModifiers();
                        // 忽略 static 或 final 字段
                        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                            continue;
                        }

                        declaredField.setAccessible(true);
                        String attribute = config.getAttribute(attributeName);
                        Object object = parseValue(attribute, declaredField.getType());
                        declaredField.set(t, object);
                    } catch (NoSuchFieldException e) {
                        // 无这个字段
                        throw new MonitorSettingException("Tag:" + name + ", attr:" + attributeName + " not found");
                    }
                }
            }

            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    };

    public Field findField(String fieldName, Class<?> fieldType) throws NoSuchFieldException {
        Class<?> aClass = fieldType;
        while (aClass != Object.class) {
            try {
                Field declaredField = aClass.getDeclaredField(fieldName);
                return declaredField;
            } catch (NoSuchFieldException e) {
                aClass =  aClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field:" + fieldName + " not found");
    }

    protected Object parseValue(String value, Class<?> aClass) {
        if (value == null || value.length() == 0) {
            return null;
        }
        if (aClass.equals(String.class)) {
            return value;
        }
        if (aClass.equals(Integer.class) || aClass.equals(int.class)) {
            return Integer.parseInt(value);
        }
        if (aClass.equals(Long.class) || aClass.equals(long.class)) {
            return Long.parseLong(value);
        }
        if (aClass.equals(Double.class) || aClass.equals(double.class)) {
            return Double.parseDouble(value);
        }
        if (aClass.equals(Boolean.class) || aClass.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        if (aClass.equals(Float.class) || aClass.equals(float.class)) {
            return Float.parseFloat(value);
        }
        if (aClass.equals(Character.class) || aClass.equals(char.class)) {
            return value.charAt(0);
        }
        if (aClass.equals(Byte.class) || aClass.equals(byte.class)) {
            return Byte.parseByte(value);
        }
        if (aClass.equals(Short.class) || aClass.equals(short.class)) {
            return Short.parseShort(value);
        }
        if (Date.class.isAssignableFrom(aClass)) {
            return new Date(Long.parseLong(value));
        }
        return null;
    }
}
