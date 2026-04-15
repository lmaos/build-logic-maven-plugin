package com.clmcat.maven.plugins.action;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionXUtilsTest {

    @Test
    void shouldHandleBasicStringHelpers() {
        assertTrue(XUtils.isEmpty(null));
        assertTrue(XUtils.isEmpty("   "));
        assertFalse(XUtils.isNotEmpty("   "));
        assertTrue(XUtils.isNumber("123"));
        assertTrue(XUtils.isNumber("123.45"));
        assertFalse(XUtils.isNumber("abc"));
        assertTrue(XUtils.isVariableName("name_1"));
        assertFalse(XUtils.isVariableName("bad.name"));
    }

    @Test
    void shouldSplitQuotedArgumentsWithoutBreakingInnerCommas() {
        assertArrayEquals(
                new String[]{"alpha", "\"beta, gamma\"", "'delta, epsilon'", "zeta"},
                XUtils.splitArguments("alpha, \"beta, gamma\", 'delta, epsilon', zeta")
        );
    }

    @Test
    void shouldResolveSimpleClassesUnquoteAndFormatDates() throws Exception {
        assertEquals(String.class, XUtils.toSimpleClass("String"));
        assertEquals(int.class, XUtils.toSimpleClass("int"));
        assertEquals(File.class, XUtils.toSimpleClass("File"));
        assertEquals("hello", XUtils.unquote("\"hello\""));
        assertEquals("world", XUtils.unquote("'world'"));
        assertEquals("plain", XUtils.unquote("plain"));
        assertEquals("1970-01-01", XUtils.formatDate(0L, "yyyy-MM-dd", "UTC"));
        assertNotNull(XUtils.parseDate("1970-01-01", "yyyy-MM-dd", "UTC"));
    }
}
