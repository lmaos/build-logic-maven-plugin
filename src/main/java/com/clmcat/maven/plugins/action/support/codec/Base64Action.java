package com.clmcat.maven.plugins.action.support.codec;

import com.clmcat.maven.plugins.action.Action;
import com.clmcat.maven.plugins.action.ActionParam;
import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.support.VariableAction;
import com.clmcat.maven.plugins.action.variable.BytesVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Action extends VariableAction {
    private String encoding = "UTF-8";

    // base64.encode
    // base64.decode
    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {

        String ref = getRef();
        Variable variable = null;
        if (XUtils.isEmpty(ref)) {
            String value = getValue();
            if (XUtils.isEmpty(value)) {
                throw new MojoExecutionException("ref is empty");
            }
            variable = StringVariable.of(value);
        } else {
            variable = getVariable(ref);
        }

        if (Variable.isNULL(variable)) {
            throw new MojoExecutionException("ref Variable not found: " + ref);
        }
        String tagMethod = getTagMethod();
        Variable resultVariable = null;
        if ("encode".equals(tagMethod)) {
            byte[] bytes = null;
            if (variable.getValue() instanceof byte[] ) {
                bytes = (byte[]) variable.getValue();
            } else if (variable.getValue() instanceof Byte[]) {
                bytes = toByteArray((Byte[]) variable.getValue());
            } else if (variable.getValue() instanceof String) {
                bytes = variable.getStringValue().getBytes(Charset.forName(encoding));
            } else if (variable.getValue() instanceof File) {
                bytes = XUtils.readFileToBytes((File) variable.getValue());
            } else {
                // 无法进行base64编码， 类型错误
                throw new MojoExecutionException("Base64 encode Unknown type: " + variable.getValue().getClass().getName());
            }
            byte[] encodedBytes = base64Encode(bytes);
            resultVariable = BytesVariable.of(encodedBytes, encoding);
        } else if ("decode".equals(tagMethod)) {
            byte[] decodedBytes = null;
            if (variable.getValue() instanceof File) {
                byte[] bytes = XUtils.readFileToBytes((File) variable.getValue());
                decodedBytes = base64Decode(bytes);
            } else if (variable.getValue() instanceof String) {
                decodedBytes = base64Decode(variable.getStringValue());
            }  else if (variable.getValue() instanceof Byte[]) {
                byte[] bytes = toByteArray((Byte[]) variable.getValue());
                decodedBytes = base64Decode(bytes);
            }  else if (variable.getValue() instanceof byte[]) {
                byte[] bytes = (byte[]) variable.getValue();
                decodedBytes = base64Decode(bytes);
            } else  {
                throw new MojoExecutionException("Base64 decode Unknown type: " + variable.getValue().getClass().getName());
            }

            resultVariable = BytesVariable.of(decodedBytes, encoding);
        } else {
            throw new MojoExecutionException("base64 tagMethod not found: " + tagMethod);
        }
        setVariable(actionParam, resultVariable);
    }

    private byte[] base64Encode(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    private byte[] base64Decode(String base64Value) {
        return Base64.getDecoder().decode(base64Value);
    }

    private byte[] base64Decode(byte[] base64Value) {
        return Base64.getDecoder().decode(base64Value);
    }

    private byte[] toByteArray(Byte[] bytes) {
        byte[] byteArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byteArray[i] = bytes[i];
        }
        return byteArray;
    }
}
