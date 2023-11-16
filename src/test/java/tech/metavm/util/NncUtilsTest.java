package tech.metavm.util;

import junit.framework.TestCase;

public class NncUtilsTest extends TestCase {

    public void testToBase64() {
        System.out.println(NncUtils.encondeBase64(1000000000));
    }

}