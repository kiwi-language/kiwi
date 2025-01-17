package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.metavm.object.type.ClassType;
import org.metavm.util.TestUtils;

public class IdTest extends TestCase {

    public void test() {
        Id id = PhysicalId.of(1L, 0L);
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
    }

    private ClassType mockClassTypeKey(long treeId, long nodeId) {
        var klass = TestUtils.newKlassBuilder("Mock", "Mock").build();
        klass.initId(new PhysicalId(treeId, nodeId));
        return klass.getType();
    }

}