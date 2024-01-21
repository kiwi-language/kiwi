package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;

public class IdTest extends TestCase {

    public void test() {
        Id id = new PhysicalId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(1L, new PhysicalId(2L));
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = new PhysicalId(1L);
        ViewId rootViewId = new DefaultViewId(10L, rootId);

        Id sourceId = new PhysicalId(2L);
        ChildViewId childId = new ChildViewId(11L, sourceId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var parentId = new DefaultViewId(100L, PhysicalId.of(1L));
        Id id = new FieldViewId(parentId, 1L, 2L);
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var parentId = new DefaultViewId(100L ,PhysicalId.of(1L));

        Id id = new ElementViewId(parentId, 1L, 2);
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }


}