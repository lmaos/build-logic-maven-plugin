package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.variable.BytesVariable;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class ReadAction extends VariableAction  {

    private String file;
    private String encoding = "UTF-8";

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String encoding = this.encoding;
        encoding = encoding == null ? "UTF-8" : encoding;
        String file = actionParam.format(this.file);
        final Variable variable = getVariable(file);
        final File readFile;
        if (variable instanceof FileVariable) {
            readFile = ((FileVariable)variable).getValue();
        } else {
            readFile = new File(file);
        }

        if (!readFile.exists()) {
            throw new MojoExecutionException("Read File " + readFile + " does not exist");
        }

        try (FileInputStream fis = new FileInputStream(readFile)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            byte[] data = bos.toByteArray();
            setVariable(actionParam, new BytesVariable(data, encoding));
            actionParam.info("Read File " + readFile + " success");
        }
    };
}
