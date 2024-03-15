package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;

public class IdTest extends TestCase {

    public void test() {
        Id id = PhysicalId.ofClass(1L, 2L);
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(PhysicalId.ofClass(1L, 3L), PhysicalId.ofClass(4L, 5L));
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = PhysicalId.ofClass(1L, 2L);
        ViewId rootViewId = new DefaultViewId(PhysicalId.ofClass(3L, 4L), rootId);

        Id sourceId = PhysicalId.ofClass(5L, 6L);
        ChildViewId childId = new ChildViewId(PhysicalId.ofClass(7L, 8L), sourceId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var parentId = new DefaultViewId(PhysicalId.ofClass(3L, 3L), PhysicalId.ofClass(1L, 2L));
        Id id = new FieldViewId(parentId, PhysicalId.ofClass(4L, 4L), PhysicalId.ofClass(5L, 6L), null,
                PhysicalId.ofClass(7L, 8L));
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var parentId = new DefaultViewId(PhysicalId.ofClass(100L, 3L) ,PhysicalId.ofClass(1L, 2L));

        Id id = new ElementViewId(parentId, PhysicalId.ofClass(1L, 2L), 2, null,
                PhysicalId.ofClass(3L, 5L));
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }


}