package com.clmcat.maven.plugins;

import com.clmcat.maven.plugins.action.ActionExecute;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.compare.Compare;
import com.clmcat.maven.plugins.action.compare.CompareFactory;
import com.clmcat.maven.plugins.action.compare.LogicCompare;
import com.clmcat.maven.plugins.action.execute.DefaultActionExecute;
import com.clmcat.maven.plugins.action.factory.DefaultActionFactory;
import com.clmcat.maven.plugins.action.support.*;
import com.clmcat.maven.plugins.action.support.codec.Base64Action;
import com.clmcat.maven.plugins.action.support.string.StringAction;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import com.clmcat.maven.plugins.action.variable.FunctionVariablesReference;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.inject.Inject;
import java.io.File;

/**
 * Custom build logic plugin.
 * Run: mvn com.clmcat.maven.plugins:build-logic-maven-plugin:1.0.0:run
 */
@Mojo(name = "run") // Plugin goal: run
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
        DefaultActionFactory actionFactory = new DefaultActionFactory();
        actionFactory.addActionType("main", MainAction.class); // main entry point
        actionFactory.addActionType("echo", EchoAction.class); // print output
        actionFactory.addActionType("var", VariableAction.class); // create variable
        actionFactory.addActionType("file", FileAction.class); // file variable
        actionFactory.addActionType("write", WriteAction.class); // write operation
        actionFactory.addActionType("read", ReadAction.class);
        actionFactory.addActionType("mkdir", MkdirAction.class);
        actionFactory.addActionType("zip", ZipAction.class);

        actionFactory.addActionType("delete", DeleteAction.class);
        actionFactory.addActionType("func", FuncAction.class);
        actionFactory.addActionType("call", CallAction.class);
        actionFactory.addActionType("return", ReturnAction.class);

        actionFactory.addActionType("if", IfAction.class);
        actionFactory.addActionType("foreach", ForEachAction.class);
        actionFactory.addActionType("list", ListAction.class);
        actionFactory.addActionType("http", HttpAction.class);
        actionFactory.addActionType("str", StringAction.class);
        actionFactory.addActionType("base64", Base64Action.class);
        actionFactory.addActionType("date", DateAction.class);

        this.actionExecute = new DefaultActionExecute(actionFactory);
    }


    // ====================== Execution logic ======================
    @Override
    public void execute() throws MojoFailureException {

        getLog().info("Build Logic Mojo: " + project);

        if (allowWriteDir == null) {
            allowWriteDir = project.getBasedir();
        }
        try {

            FunctionVariablesReference.execute(null,(functionVariable) -> {
                Compare compare = new LogicCompare();

                if (this.compare != null && this.compare.isEmpty() == false) {
                    compare = CompareFactory.newInstance(this.compare);
                }

                ActionParam actionParam = new ActionParam(this, this.actionExecute, project, compare, functionVariable);

                actionParam.setVariable("allowWriteDir", new FileVariable(this.allowWriteDir));
                actionParam.setVariable("basedir", new FileVariable(project.getBasedir()));
                actionParam.setVariable("project.basedir", new FileVariable(project.getBasedir()));

                actionExecute.executeAction(actionParam, main);
            });
        } catch (Exception e) {
            getLog().error("ERROR: Build Logic Mojo: " + project, e);
            throw new MojoFailureException( e);
        }


    }
}