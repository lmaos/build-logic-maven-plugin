package com.clmcat.test;

import com.clmcat.maven.plugins.action.XUtils;
import org.junit.jupiter.api.Test;

public class XUtilsTest {

    @Test
    public void testIsNumber(){
        assert XUtils.isNumber("123");
        assert XUtils.isNumber("123.456");
//        assert XUtils.isNumber(".123.456");
    }


    @Test
    void testRegex(){
        // test whether the regex matches

        String pattern = "*.txt";

        System.out.println("asd.1txt".matches(pattern.replace(".","\\.").replace("*",".*")));
    }
}
