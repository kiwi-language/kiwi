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

}