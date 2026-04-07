package com.clmcat.maven.plugins.action.execute;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionExecute;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class DefaultActionExecute implements ActionExecute {
    private ActionFactory actionFactory;

    public DefaultActionExecute(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    @Override
    public void executeAction(ActionParam actionParam, PlexusConfiguration config) throws Exception {
        Action action = actionFactory.newInstance(config);

        if (action != null) {
            action.execute(actionParam, null);
        }
    }


}
