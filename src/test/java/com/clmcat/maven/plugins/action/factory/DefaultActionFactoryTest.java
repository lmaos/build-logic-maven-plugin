package com.clmcat.maven.plugins.action.factory;

import com.clmcat.maven.plugins.action.support.EchoAction;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultActionFactoryTest {

    @Test
    void shouldBindDottedTagAndPrivateField() throws Exception {
        DefaultActionFactory factory = new DefaultActionFactory();
        factory.addActionType("echo", EchoAction.class);

        EchoAction action = factory.newInstance(configuration("echo.warn", "hello", attributes("level", "warn")));

        assertEquals("echo.warn", action.getTag());
        assertEquals("warn", action.getTagMethod());
        assertEquals("warn", readField(action, "level"));
    }

    @Test
    void shouldRejectUnknownAttribute() {
        DefaultActionFactory factory = new DefaultActionFactory();
        factory.addActionType("echo", EchoAction.class);

        MojoExecutionException exception = assertThrows(MojoExecutionException.class,
                () -> factory.newInstance(configuration("echo", "hello", attributes("missing", "value"))));

        assertTrue(exception.getMessage().contains("Unknown attribute 'missing' on tag <echo>"));
    }

    @Test
    void shouldRejectUnknownActionTag() {
        DefaultActionFactory factory = new DefaultActionFactory();

        MojoExecutionException exception = assertThrows(MojoExecutionException.class,
                () -> factory.newInstance(configuration("missing", "value", Collections.emptyMap())));

        assertTrue(exception.getMessage().contains("Unknown action tag: <missing>"));
    }

    private PlexusConfiguration configuration(String name, String value, Map<String, String> attributes,
                                               PlexusConfiguration... children) {
        final Map<String, String> attributeMap = attributes == null
                ? Collections.<String, String>emptyMap()
                : new LinkedHashMap<>(attributes);
        final PlexusConfiguration[] childArray = children == null ? new PlexusConfiguration[0] : children.clone();

        InvocationHandler handler = (proxy, method, args) -> invokeConfiguration(method, args, name, value, attributeMap, childArray);
        return (PlexusConfiguration) Proxy.newProxyInstance(
                DefaultActionFactoryTest.class.getClassLoader(),
                new Class<?>[]{PlexusConfiguration.class},
                handler
        );
    }

    private Object invokeConfiguration(Method method, Object[] args, String name, String value,
                                       Map<String, String> attributes, PlexusConfiguration[] children) {
        String methodName = method.getName();
        if ("getName".equals(methodName)) {
            return name;
        }
        if ("getValue".equals(methodName)) {
            return value;
        }
        if ("getAttributeNames".equals(methodName)) {
            return attributes.keySet().toArray(new String[0]);
        }
        if ("getAttribute".equals(methodName)) {
            return attributes.get(args[0]);
        }
        if ("getChildren".equals(methodName)) {
            if (args == null || args.length == 0) {
                return children.clone();
            }
            return filterChildren(String.valueOf(args[0]), children);
        }
        if ("getChild".equals(methodName)) {
            PlexusConfiguration[] matched = filterChildren(String.valueOf(args[0]), children);
            return matched.length == 0 ? null : matched[0];
        }
        return defaultValue(method.getReturnType());
    }

    private PlexusConfiguration[] filterChildren(String name, PlexusConfiguration[] children) {
        List<PlexusConfiguration> matched = new ArrayList<>();
        for (PlexusConfiguration child : children) {
            if (name.equals(child.getName())) {
                matched.add(child);
            }
        }
        return matched.toArray(new PlexusConfiguration[0]);
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        return null;
    }

    private Map<String, String> attributes(String key, String value) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(key, value);
        return attributes;
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Class<?> type = target.getClass();
        while (type != Object.class) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
