package com.clmcat.maven.plugins.action.support.string;

import com.clmcat.maven.plugins.action.Variable;
import com.clmcat.maven.plugins.action.XUtils;
import com.clmcat.maven.plugins.action.variable.ListVariable;

import java.util.Arrays;
import java.util.List;
/**
 * 字符串分割方法实现
 * <p>
 * 用于将字符串按照指定的分隔符分割成字符串列表
 * </p>
 */
public class SplitStringMethod implements StringAction.StringMethod {

    @Override
    public String[] paserParams(String _params) {
        if (XUtils.isEmpty(_params)) {
            return null;
        }
        return new String[]{_params};
    }
    /**
     * 处理字符串分割操作
     *
     * @param stringAction StringAction 实例，包含操作相关信息
     * @param params 方法参数数组，第一个元素为分割符
     * @param variable 要处理的变量
     * @return 分割后的字符串列表变量
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public Variable handler(StringAction stringAction, String[] params, Variable variable) throws Exception {
        String stringValue = variable.getStringValue();
        if (params == null || params.length == 0) {
            return ListVariable.of(Arrays.asList(stringValue));
        }
        String name = stringAction.getName();
        String tag = stringAction.getTag();
        String splitParam = params[0];
        String[] array = stringValue.split(splitParam);
        List<String> list = Arrays.asList(array);
        return ListVariable.of(list);
    }
}
