package com.clmcat.maven.plugins;

import com.clmcat.maven.plugins.action.ActionExecute;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.compare.Compare;
import com.clmcat.maven.plugins.action.compare.CompareFactory;
import com.clmcat.maven.plugins.action.compare.ExpressionCalculatorCompare;
import com.clmcat.maven.plugins.action.execute.DefaultActionExecute;
import com.clmcat.maven.plugins.action.factory.ActionFactories;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.inject.Inject;
import java.io.File;

/**
 * 自定义构建逻辑插件
 * 执行：mvn com.clmcat.maven.plugins:build-logic-maven-plugin:1.0.0:run
 */
@Mojo(name = "run") // 插件命令：run
public class RunBuildLogicMojo extends AbstractMojo {

    @Parameter
    private PlexusConfiguration main;
    @Parameter
    private String compare;

    private ActionExecute actionExecute;

    /**
     * The Maven project object
     */
    @Inject
    private MavenProject project;

    @Parameter()
    private File allowWriteDir;

    public RunBuildLogicMojo() {
        this.actionExecute = new DefaultActionExecute(ActionFactories.createRootActionFactory());
    }


    // ====================== 执行逻辑 ======================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("Build Logic Mojo: " + project);
        if (main == null) {
            throw new MojoExecutionException("Build logic plugin requires <main> configuration");
        }

        if (allowWriteDir == null) {
            allowWriteDir = project.getBasedir();
        }
        try {

            FunctionVariablesReference.execute(null,(functionVariable) -> {
                Compare compare = new ExpressionCalculatorCompare();

                if (this.compare != null && this.compare.isEmpty() == false) {
                    compare = CompareFactory.newInstance(this.compare);
                }

                ActionParam actionParam = new ActionParam(this, this.actionExecute, project, compare, functionVariable);

                actionParam.setVariable("allowWriteDir", new FileVariable(this.allowWriteDir));
                actionParam.setVariable("basedir", new FileVariable(project.getBasedir()));
                actionParam.setVariable("project.basedir", new FileVariable(project.getBasedir()));

                actionExecute.executeAction(actionParam, main);
            });
        } catch (MojoExecutionException exception) {
            throw exception;
        } catch (Exception e) {
            getLog().error("ERROR: Build Logic Mojo: " + project, e);
            throw new MojoFailureException("Build Logic Mojo execution failed", e);
        }


    }
}
