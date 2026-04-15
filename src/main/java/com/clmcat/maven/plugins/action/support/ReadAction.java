package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionFileSupport;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.variable.BytesVariable;

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
        File readFile = ActionFileSupport.resolveFile(this, actionParam, this.file, "file", true);
        ActionFileSupport.ensureRegularFile(readFile, "Read file");

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
