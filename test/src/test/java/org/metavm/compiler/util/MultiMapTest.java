package org.metavm.compiler.util;

import junit.framework.TestCase;
import org.junit.Assert;

public class MultiMapTest extends TestCase  {

    private record Foo(String value) {}

    public void test() {
        var map = new MultiMap<String, Foo>();
        var v1 = new Foo("v1");
        var v2 = new Foo("v2");
        var v11 = new Foo("v11");

        map.put("v1", v1);
        Assert.assertSame(v1, map.getFirst("v1"));

        map.put("v2", v2);
        Assert.assertSame(v2, map.getFirst("v2"));

        map.put("v1", v11);
        Assert.assertSame(v11, map.getFirst("v1"));

        Assert.assertEquals(3, map.size());

        var it = map.get("v1", v -> true).iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(v11, it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(v1, it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertTrue(map.remove("v1", v11));
        Assert.assertSame(v1, map.getFirst("v1"));

        Assert.assertTrue(map.remove("v2", v2));
        Assert.assertNull(map.getFirst("v2"));
        Assert.assertFalse(map.get("v2", v -> true).iterator().hasNext());

        Assert.assertFalse(map.remove("v2", v2));

        Assert.assertNull(map.getFirst("v1", v -> v.value.equals("v2")));
    }

    public void testGrow() {
        var map = new MultiMap<String, Foo>();
        for (int i = 0; i < 100; i++) {
            var key = "t" + i;
            for (int j = 0; j < 10; j++) {
                map.put(key, new Foo(key + "_" + j));
            }
        }
        Assert.assertEquals(1000, map.size());
        for (int i = 0; i < 100; i++) {
            var key = "t" + i;
            var value = map.getFirst(key);
            Assert.assertNotNull(value);
            Assert.assertEquals(key + "_9", value.value);
        }
    }

    public void testRemoveDuringIteration() {
        var map = new MultiMap<String, Foo>();
        int n = 5;
        var foos = new Foo[5];
        for (int i = 0; i < n; i++) {
            foos[i] = new Foo("foo_" + i);
            map.put("foo", foos[i]);
        }
        var it = map.get("foo", e -> true).iterator();
        var removed = foos[3];
        map.remove("foo", removed);
        int cnt = 0;
        while (it.hasNext()) {
            it.next();
            cnt++;
        }
        Assert.assertEquals(5, cnt);
    }

    public void testForEach() {
        var f1 = new Foo("f1");
        var f2 = new Foo("f2");
        var f3 = new Foo("f3");
        var map = new MultiMap<String, Foo>();
        map.put("f", f1);
        map.put("f", f2);
        map.put("f", f3);

        var sb = new StringBuilder();
        map.forEach((k, v) -> {
            if (!sb.isEmpty())
                sb.append(",");
            sb.append(v.value);
        });
        Assert.assertEquals("f3,f2,f1", sb.toString());
    }

    public void testPutAll() {
        var map = new MultiMap<String, Foo>();
        var f1 = new Foo("f1");
        map.put("f1", f1);
        map.put("f2", new Foo("f2"));

        var map1 = new MultiMap<String, Foo>();
        var f11 = new Foo("f1-1");
        map1.put("f1", f11);
        map1.put("f2", new Foo("f2-1"));
        map1.put("f3", new Foo("f3-1"));

        map.putAll(map1);

        Assert.assertSame(f11, map.getFirst("f1"));

        map.removeAll(map1);

        Assert.assertSame(f1, map.getFirst("f1"));
        Assert.assertNull(map.getFirst("f3"));

        var largeMap = new MultiMap<String, Foo>();
        var n = 100;
        for (int i = 0; i < n; i++) {
            largeMap.put("key" + i, new Foo("foo" + i));
        }
        map.putAll(largeMap);

        for (int i = 0; i < n; i++) {
            var f = map.getFirst("key" + i);
            Assert.assertNotNull(f);
            Assert.assertEquals("foo" + i, f.value);
        }
    }

}
