package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.ClassTypeBuilder;
import org.metavm.object.type.rest.dto.AnyTypeKey;
import org.metavm.object.view.rest.dto.DirectMappingKey;

public class IdTest extends TestCase {

    public void test() {
        Id id = PhysicalId.of(1L, 0L, mockClassTypeKey(2, 3));
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(false, new DirectMappingKey(PhysicalId.of(4L, 1L, mockClassTypeKey(4, 0))), id);
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = PhysicalId.of(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(PhysicalId.of(4L, 1L, mockClassTypeKey(4, 0)));
        ViewId rootViewId = new DefaultViewId(false, mappingKey, rootId);

        ChildViewId childId = new ChildViewId(false, mappingKey, rootId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var sourceId = PhysicalId.of(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(PhysicalId.of(4L, 1L, mockClassTypeKey(4, 0)));
        var parentId = new DefaultViewId(false, mappingKey, sourceId);

        var mappingId2 = new DirectMappingKey(PhysicalId.of(5L, 1L, mockClassTypeKey(4, 0)));
        var fieldId = PhysicalId.of(6L, 1L, mockClassTypeKey(4, 0));
        Id id = new FieldViewId(false, parentId, mappingId2, fieldId, null, new AnyTypeKey());
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var sourceId = PhysicalId.of(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(PhysicalId.of(4L, 1L, mockClassTypeKey(4, 0)));
        var parentId = new DefaultViewId(false, mappingKey, sourceId);
        var type = mockClassTypeKey(10, 0);

        var mappingId2 = new DirectMappingKey(PhysicalId.of(5L, 1L, mockClassTypeKey(4, 0)));
        Id id = new ElementViewId(false, parentId, mappingId2, 2, null, type.toTypeKey());
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }

    private ClassType mockClassTypeKey(long treeId, long nodeId) {
        var klass = ClassTypeBuilder.newBuilder("Mock", "Mock").build();
        klass.initId(new PhysicalId(false, treeId, nodeId));
        return klass.getType();
    }

}