package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

public class NamingUtilsTest extends TestCase {

    public void testEnsureValidTypeCode() {
        NamingUtils.ensureValidTypeName("[Never,Element]");
        NamingUtils.ensureValidTypeName("String&Integer");
        NamingUtils.ensureValidTypeName("String|Integer");
        NamingUtils.ensureValidTypeName("String[]");
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
            var path = "user-service";
            Assert.assertEquals("UserService", NamingUtils.pathToName(path, true));
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

    public void testNameToLabel() {
        assertEquals("Product Name", NamingUtils.nameToLabel("productName"));
        assertEquals("Order Placement Service", NamingUtils.nameToLabel("OrderPlacementService"));
        assertEquals("SKU", NamingUtils.nameToLabel("SKU"));
    }

    public void testFirstCharsToLowerCase() {
        assertEquals("product", NamingUtils.firstCharsToLowerCase("Product"));
        assertEquals("sku", NamingUtils.firstCharsToLowerCase("SKU"));
        assertEquals("skulist", NamingUtils.firstCharsToLowerCase("SKUList"));
    }

}