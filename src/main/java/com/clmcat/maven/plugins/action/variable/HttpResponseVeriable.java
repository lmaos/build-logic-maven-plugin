package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.support.HttpAction;

public class HttpResponseVeriable extends Variable.AbstractVariable<HttpAction.HttpResponse> {
    public HttpResponseVeriable(HttpAction.HttpResponse value) {
        super(value);
    }
}
