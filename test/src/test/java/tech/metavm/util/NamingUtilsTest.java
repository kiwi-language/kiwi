package tech.metavm.util;

import junit.framework.TestCase;

public class NamingUtilsTest extends TestCase {

    public void testEnsureValidTypeCode() {
        NamingUtils.ensureValidTypeCode("[Never,Element]");
        NamingUtils.ensureValidTypeCode("String&Integer");
        NamingUtils.ensureValidTypeCode("String|Integer");
        NamingUtils.ensureValidTypeCode("String[]");
    }

    public void testEscape() {
        var name = "Object[]";
        System.out.println(NamingUtils.escapeTypeName(name));
    }

}