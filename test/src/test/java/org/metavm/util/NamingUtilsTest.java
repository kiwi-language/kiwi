package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

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

    public void testPathToName() {
        {
            var path = "user-service";
            Assert.assertEquals("userService", NamingUtils.pathToName(path));
        }
        {
            var path = "org/metavm/lab/user-service";
            var name = NamingUtils.pathToName(path);
            Assert.assertEquals("org.metavm.lab.UserService", name);
        }
    }

    public void testNameToPath() {
        var name = "org.metavm.lab.UserService";
        var path = NamingUtils.nameToPath(name);
        Assert.assertEquals("org/metavm/lab/user-service", path);
    }

}