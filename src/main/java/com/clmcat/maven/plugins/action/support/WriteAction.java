package com.clmcat.maven.plugins.action.support;


import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionFileSupport;
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
    // 引用变量
    private String ref;

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        File writeFile = ActionFileSupport.resolveFile(this, actionParam, file, "file", false);
        if (encoding == null || (encoding = encoding.trim()).isEmpty()) {
            encoding = "UTF-8";
        }
        ActionFileSupport.requireSafeWriteTarget(this, actionParam, writeFile);
        ActionFileSupport.ensureParentDirectoryExists(writeFile, "Write target");

        actionParam.info("write file:" + writeFile.getAbsolutePath());
        if (!append && !overwrite && writeFile.exists()) {
            actionParam.info("File already exists, skip writing: " + writeFile);
            return;
        }

        if (XUtils.isEmpty(ref)) {
            String value = getValue();
            byte[] contentBytes = XUtils.toBytes(value, encoding);
            try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                fos.write(contentBytes);
                fos.flush();
            }
        } else {
            Variable refVariable = getVariable(ref);
            if (!Variable.isExist(refVariable)) {
                throw new MojoExecutionException("<write ref=\"" + ref + "\"> variable does not exist");
            }
            if (refVariable instanceof FileVariable) {
                File refRead = ((FileVariable) refVariable).getValue();
                ActionFileSupport.ensureRegularFile(refRead, "Write ref file");
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
                try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                    fos.write(((BytesVariable) refVariable).getValue());
                    fos.flush();
                }
            } else {
                try (FileOutputStream fos = new FileOutputStream(writeFile, append)) {
                    fos.write(XUtils.toBytes(refVariable.getStringValue(), encoding));
                    fos.flush();
                }
            }
        }
        if (append) {
            actionParam.info("append, write file:" + writeFile.getAbsolutePath());
        } else {
            actionParam.info("success, write file:" + writeFile.getAbsolutePath());
        }
    }
}
