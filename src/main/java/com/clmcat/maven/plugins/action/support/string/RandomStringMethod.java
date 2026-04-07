package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.ListVariable;
import com.clmcat.maven.plugins.action.variable.StringVariable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Random;

public class RandomStringMethod implements StringAction.StringMethod {
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {

        if (params == null || params.length == 0) {
            throw new MojoExecutionException("params is empty");
        } else if (params.length > 3) {
            throw new MojoExecutionException("params is too long");
        }

        Random random = new Random();
        int count = 0;
        String stringValue = variable.getStringValue();
        String splitParam = null;
        if (params.length == 1) {
            count = Integer.parseInt(params[0]);
        } else if (params.length >= 2) {
            int min = Integer.parseInt(params[0]);
            int max = Integer.parseInt(params[1]);
            // 随机总数
            count = max <= min ? min : random.nextInt(max - min + 1)  + min;
        }


        if (params.length == 3) {
            splitParam = params[2];
        }

        StringBuffer stringBuffer = new StringBuffer();
        if (variable instanceof ListVariable) {
            ListVariable  listVariable = (ListVariable) variable;
            for (int i = 0; i < count; i++) {
                stringBuffer.append(listVariable.get(random.nextInt(listVariable.size())));
            }
        } else if (XUtils.isNotEmpty(splitParam)) {
            String[] array = stringValue.split(splitParam);
            for (int i = 0; i < count && stringBuffer.length() < count; i++) {
                stringBuffer.append(array[random.nextInt(array.length)]);
            }

        } else {
            for (int i = 0; i < count; i++) {
                stringBuffer.append(stringValue.charAt(random.nextInt(stringValue.length())));
            }
        }
        if (stringBuffer.length() > count) {

            stringBuffer.setLength(count);
        }
        return StringVariable.of(stringBuffer);
    }
}
