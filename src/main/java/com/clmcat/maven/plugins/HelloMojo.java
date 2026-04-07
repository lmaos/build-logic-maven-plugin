package com.clmcat.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "hello")
public class HelloMojo extends AbstractMojo {
    @Parameter
    private String name;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("** Hello World, name = " + name);
    }
}
