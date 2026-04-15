package com.clmcat.maven.plugins.action.factory;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.DeferredChildrenParsingAction;
import com.clmcat.maven.plugins.action.GroupAction;
import com.clmcat.maven.plugins.action.anns.NotAttr;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.io.File;
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
    public <T extends Action> T newInstance(PlexusConfiguration config) throws MojoExecutionException {
        if (config == null) {
            throw new MojoExecutionException("Action configuration is null");
        }
        Class<?> aClass = actionTypeMap.get(getTagName(config.getName()).toLowerCase());
        if (aClass == null) {
            throw new MojoExecutionException("Unknown action tag: <" + config.getName() + ">");
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

    public <T extends Action> T newInstance(PlexusConfiguration config , Class<?> actionClass) throws MojoExecutionException {

        try {
            String name = config.getName();
            T t = (T) actionClass.getDeclaredConstructor().newInstance();
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
            if (t instanceof GroupAction && !(t instanceof DeferredChildrenParsingAction)) {
                PlexusConfiguration[] children = config.getChildren();
                if (children != null && children.length > 0) {
                    for (PlexusConfiguration childConfig : children) {
                        Action childAction = newInstance(childConfig);
                        ((GroupAction) t).addAction(childAction);
                    }
                }
            }

            String[] attributeNames = config.getAttributeNames();
            if (attributeNames != null && attributeNames.length > 0) {
                for (String attributeName : attributeNames) {
                    bindAttribute(t, actionClass, name, config, attributeName);
                }
            }

            return t;
        } catch (MojoExecutionException exception) {
            throw exception;
        } catch (Exception e) {
            String tagName = config == null ? "<unknown>" : config.getName();
            throw new MojoExecutionException("Failed to create action for tag: <" + tagName + ">", e);
        }

    }

    private void bindAttribute(Action action, Class<?> actionClass, String tagName,
                               PlexusConfiguration config, String attributeName) throws Exception {
        Field declaredField;
        try {
            declaredField = findField(attributeName, actionClass);
        } catch (NoSuchFieldException exception) {
            throw new MojoExecutionException("Unknown attribute '" + attributeName + "' on tag <" + tagName + ">", exception);
        }

        if (declaredField.isAnnotationPresent(NotAttr.class)) {
            return;
        }

        int modifiers = declaredField.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
            return;
        }

        declaredField.setAccessible(true);
        String attribute = config.getAttribute(attributeName);
        Object object = parseValue(attribute, declaredField.getType(), tagName, attributeName);
        declaredField.set(action, object);
    }

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

    protected Object parseValue(String value, Class<?> aClass, String tagName, String attributeName) throws MojoExecutionException {
        if (value == null || value.length() == 0) {
            return null;
        }
        try {
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
                if (value.length() != 1) {
                    throw new IllegalArgumentException("expected a single character");
                }
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
            if (File.class.isAssignableFrom(aClass)) {
                return new File(value);
            }
        } catch (Exception exception) {
            throw new MojoExecutionException("Failed to parse attribute '" + attributeName + "' on tag <" + tagName
                    + "> as " + aClass.getSimpleName() + ": " + value, exception);
        }
        throw new MojoExecutionException("Unsupported attribute type " + aClass.getName() + " on tag <" + tagName
                + "> for attribute '" + attributeName + "'");
    }
}
