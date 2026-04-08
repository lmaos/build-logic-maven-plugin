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
        DefaultActionFactory actionFactory = new DefaultActionFactory();
        actionFactory.addActionType("main", MainAction.class); // main 主函数
        actionFactory.addActionType("echo", EchoAction.class); // 输出信息
        actionFactory.addActionType("var", VariableAction.class); // 创建变量
        actionFactory.addActionType("file", FileAction.class); // 文件变量
        actionFactory.addActionType("write", WriteAction.class); // 写操作
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


    // ====================== 执行逻辑 ======================
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