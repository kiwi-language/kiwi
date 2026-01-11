package org.manul.object.instance.search;

import junit.framework.TestCase;
import org.manul.compiler.util.List;

public class SearchUtilTest extends TestCase {

    public void testTokenize() {
        assertEquals(
                List.of("this", "is", "manul"),
                SearchUtil.tokenize("This is Manul")
        );
    }

    public void testMatch() {
        assertTrue(
                SearchUtil.match("This is Manul", "this manul")
        );
        assertFalse(
                SearchUtil.match("This is Manul", "manul this")
        );
        assertTrue(
                SearchUtil.match("manul-objects", "manul objects")
        );
        assertFalse(
                SearchUtil.match("manul_objects", "manul objects")
        );
        assertTrue(
                SearchUtil.match("Foo001", "Foo001")
        );
    }

    public void testPrefixMatch() {
        assertTrue(
                SearchUtil.prefixMatch("This is Manul", "man")
        );
        assertFalse(
                SearchUtil.prefixMatch("This is Manul", "This is man")
        );
        assertTrue(
                SearchUtil.prefixMatch("Foo001", "Foo001")
        );
    }

}