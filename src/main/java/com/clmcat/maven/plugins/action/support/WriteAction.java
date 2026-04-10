package com.clmcat.maven.plugins.action.support;


import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.BytesVariable;
import com.clmcat.maven.plugins.action.variable.FileVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

public class WriteAction extends Action.AbstractAction {

    private String file;
    private boolean append;
    private boolean overwrite;
    private String encoding = "UTF-8";
    // referenced variable
    private String ref;

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String _file = actionParam.format(file);

        Variable variable = getVariable(_file);
        if (encoding == null || (encoding = encoding.trim()).isEmpty()) {
            encoding = "UTF-8";
        }

        if (variable instanceof FileVariable) {
            File writeFile = ((FileVariable)variable).getValue();
            if (writeFile == null) {
                throw new MojoExecutionException("File variable value is null, file:" + writeFile);
            }

            actionParam.info("write file:" + writeFile.getAbsolutePath());

            if (!append && !overwrite && writeFile.exists()) {
                actionParam.info("File already exists, skip writing: " + writeFile);
                return;
            }
            // write to file
            if(XUtils.isEmpty(ref)) {

                try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                    fos.write(getValue().getBytes(Charset.forName(encoding)));
                    fos.flush();
                }
            } else {
                Variable refVariable = getVariable(ref);
                if (refVariable instanceof FileVariable) {
                    File refRead = ((FileVariable)refVariable).getValue();
                    if (refRead == null || !refRead.exists()) {
                        throw new MojoExecutionException("<write ref = " + ref + " >, File not exist, file:" + refRead);
                    }
                    try (FileOutputStream fos = new FileOutputStream(writeFile, append);
                         FileInputStream fis = new FileInputStream(refRead)) {
                        int len;
                        byte[] buf = new byte[1024];
                        while ((len = fis.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        fos.flush();
                    }
                } else if (refVariable instanceof BytesVariable) {

                    if (!refVariable.isExist()) {
                        throw new MojoExecutionException("<write ref = " + ref + " >, BytesVariable not exist");
                    }

                    try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                        fos.write(((BytesVariable)refVariable).getValue());
                        fos.flush();
                    }
                } else {
                    if (!refVariable.isExist()) {
                        throw new MojoExecutionException("<write ref = " + ref + " >, not exist");
                    }
                    try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                        fos.write(refVariable.getStringValue().getBytes(Charset.forName(encoding)));
                        fos.flush();
                    }
                }
            }
            if (append) {
                actionParam.info("append, write file:" + writeFile.getAbsolutePath());
            } else {
                actionParam.info("success, write file:" + writeFile.getAbsolutePath());
            }
        } else {
            throw new MojoExecutionException("Not a file variable, file:" + file);
        }
    }
}
