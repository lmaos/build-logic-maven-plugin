package com.clmcat.maven.plugins.action.factory;

import com.clmcat.maven.plugins.action.Action;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public interface ActionFactory  {
    <T extends Action> T newInstance(PlexusConfiguration config);

    ActionFactory copy();
    ActionFactory create();

    ActionFactory addActionType(String name, Class<?> actionType);

}
