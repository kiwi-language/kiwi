package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.rest.dto.AnyTypeKey;
import tech.metavm.object.view.rest.dto.DirectMappingKey;
import tech.metavm.util.TestUtils;

public class IdTest extends TestCase {

    public void test() {
        Id id = DefaultPhysicalId.ofObject(1L, 0L, mockClassTypeKey(2, 3));
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(false, new DirectMappingKey(DefaultPhysicalId.ofObject(4L, 1L, mockClassTypeKey(4, 0)).toString()), id);
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = DefaultPhysicalId.ofObject(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(DefaultPhysicalId.ofObject(4L, 1L, mockClassTypeKey(4, 0)).toString());
        ViewId rootViewId = new DefaultViewId(false, mappingKey, rootId);

        ChildViewId childId = new ChildViewId(false, mappingKey, rootId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var sourceId = DefaultPhysicalId.ofObject(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(DefaultPhysicalId.ofObject(4L, 1L, mockClassTypeKey(4, 0)).toString());
        var parentId = new DefaultViewId(false, mappingKey, sourceId);

        var mappingId2 = new DirectMappingKey(DefaultPhysicalId.ofObject(5L, 1L, mockClassTypeKey(4, 0)).toString());
        var fieldId = DefaultPhysicalId.ofObject(6L, 1L, mockClassTypeKey(4, 0));
        Id id = new FieldViewId(false, parentId, mappingId2, fieldId, null, new AnyTypeKey());
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var sourceId = DefaultPhysicalId.ofObject(1L, 0L, mockClassTypeKey(2, 3));
        var mappingKey = new DirectMappingKey(DefaultPhysicalId.ofObject(4L, 1L, mockClassTypeKey(4, 0)).toString());
        var parentId = new DefaultViewId(false, mappingKey, sourceId);
        var type = mockClassTypeKey(10, 0);

        var mappingId2 = new DirectMappingKey(DefaultPhysicalId.ofObject(5L, 1L, mockClassTypeKey(4, 0)).toString());
        Id id = new ElementViewId(false, parentId, mappingId2, 2, null, type.toTypeKey());
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }

    private ClassType mockClassTypeKey(long treeId, long nodeId) {
//        return new ClassTypeKey(TaggedPhysicalId.ofClass(treeId, nodeId).toString());
        return TestUtils.mockClassType();
    }

}