package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;

public class IdTest extends TestCase {

    public void test() {
        Id id = DefaultPhysicalId.of(1L, 0L, TypePhysicalId.of(2L, 3L, TypeTag.CLASS));
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(DefaultPhysicalId.of(4L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS)), id);
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = DefaultPhysicalId.of(1L, 0L, TypePhysicalId.of(2L, 3L, TypeTag.CLASS));
        var mappingId = DefaultPhysicalId.of(4L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        ViewId rootViewId = new DefaultViewId(mappingId, rootId);

        ChildViewId childId = new ChildViewId(mappingId, rootId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var sourceId = DefaultPhysicalId.of(1L, 0L, TypePhysicalId.of(2L, 3L, TypeTag.CLASS));
        var mappingId = DefaultPhysicalId.of(4L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        var parentId = new DefaultViewId(mappingId, sourceId);

        var mappingId2 = DefaultPhysicalId.of(5L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        var fieldId = DefaultPhysicalId.of(6L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        var typeId = TypePhysicalId.of(10L, 0L, TypeTag.CLASS);
        Id id = new FieldViewId(parentId, mappingId2, fieldId, null, typeId);
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var sourceId = DefaultPhysicalId.of(1L, 0L, TypePhysicalId.of(2L, 3L, TypeTag.CLASS));
        var mappingId = DefaultPhysicalId.of(4L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        var parentId = new DefaultViewId(mappingId, sourceId);
        var typeId = TypePhysicalId.of(10L, 0L, TypeTag.CLASS);

        var mappingId2 = DefaultPhysicalId.of(5L, 1L, TypePhysicalId.of(4L, 0L, TypeTag.CLASS));
        Id id = new ElementViewId(parentId, mappingId2, 2, null, typeId);
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }


}