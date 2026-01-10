package org.manul.compiler.util;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.util.Collection;

@Slf4j
public class ListTest extends TestCase {

    public void test() {
        var list = List.of("Hello", "World", "I", "am", "Manul");
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(5, list.size());
        Assert.assertTrue(list.contains("Manul"));
        Assert.assertEquals("Hello", list.head());
        Assert.assertEquals("World", list.tail().head());

        var subList = list.subList(2, 5);
        Assert.assertEquals(3, subList.size());
        Assert.assertEquals("I", subList.head());

        Assert.assertEquals(
                List.of("Manul", "am", "I", "World", "Hello"),
                list.reversed()
        );

        Assert.assertEquals("Manul", list.find(s -> s.startsWith("M")));

        var it = list.findAll(s -> s.length() >= 3);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("Hello", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("World", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("Manul", it.next());
        Assert.assertFalse(it.hasNext());
    }

    public void testDelete() {
        var list = List.of("a", "b", "c");
        Assert.assertEquals(List.of("a", "c"), list.delete("b"));
    }

    public void testFromCollection() {
        var l0 = List.of("Hello", "I", "am", "Manul");
        var l = List.from((Collection<String>) l0);
        Assert.assertEquals(l0, l);
    }

    public void testBuilder() {
        var builder = List.<String>builder();
        builder.concat(List.nil());
        Assert.assertTrue(builder.build().isEmpty());
    }

}
