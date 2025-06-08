package org.metavm.object.instance.search;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;

public class SearchUtilTest extends TestCase {

    public void testTokenize() {
        assertEquals(
                List.of("this", "is", "kiwi"),
                SearchUtil.tokenize("This is Kiwi")
        );
    }

    public void testMatch() {
        assertTrue(
                SearchUtil.match("This is Kiwi", "this kiwi")
        );
        assertFalse(
                SearchUtil.match("This is Kiwi", "kiwi this")
        );
        assertTrue(
                SearchUtil.match("kiwi-objects", "kiwi objects")
        );
        assertFalse(
                SearchUtil.match("kiwi_objects", "kiwi objects")
        );
    }

    public void testPrefixMatch() {
        assertTrue(
                SearchUtil.prefixMatch("This is Kiwi", "kiw")
        );
        assertFalse(
                SearchUtil.prefixMatch("This is Kiwi", "This is kiw")
        );
    }

}