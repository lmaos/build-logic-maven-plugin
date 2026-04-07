package com.clmcat.maven.plugins.action.compare;

import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.variable.FunctionVariable;

public interface Compare {

    boolean compare(String test, FunctionVariable functionVariable);

}
