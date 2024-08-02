package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.util.TestUtils;

import static org.metavm.util.BytesUtils.toIndexBytes;

public class ReferenceTest extends TestCase {

    public void testWrite() {
        var fooKlass = TestUtils.newKlassBuilder("Foo").build();
        var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType()).build();
        foo.initId(PhysicalId.of(1L, 0L, foo.getType()));
        var ref1 = foo.getReference();
        var ref2 = new Reference(null, () -> foo);
        Assert.assertEquals(ref1, ref2);
        var bytes1 = toIndexBytes(ref1);
        var bytes2 = toIndexBytes(ref2);
        Assert.assertArrayEquals(bytes1, bytes2);
    }

}