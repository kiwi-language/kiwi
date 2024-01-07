package tech.metavm.object.instance.core;

import junit.framework.TestCase;

public class IdTest extends TestCase {

    public void test() {
        Id id = new PhysicalId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new ViewId(1L, new PhysicalId(2L));
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = new PhysicalId(1L);
        ViewId rootViewId = new ViewId(10L, rootId);

        Id sourceId = new PhysicalId(2L);
        ChildViewId childId = new ChildViewId(11L, sourceId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

}