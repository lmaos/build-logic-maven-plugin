package com.clmcat.maven.plugins.action;

import com.clmcat.maven.plugins.action.factory.ActionFactory;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public interface ActionExecute {




    public void executeAction(ActionParam actionParam, PlexusConfiguration config) throws Exception;

    public ActionFactory  getActionFactory();
}
