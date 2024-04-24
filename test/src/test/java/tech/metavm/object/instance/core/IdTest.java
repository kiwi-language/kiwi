package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.rest.dto.AnyTypeKey;
import tech.metavm.object.type.rest.dto.ClassTypeKey;

public class IdTest extends TestCase {

    public void test() {
        Id id = DefaultPhysicalId.ofObject(1L, 0L, new ClassTypeKey("2_3"));
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
        id = new DefaultViewId(false, DefaultPhysicalId.ofObject(4L, 1L, new ClassTypeKey("4_0")), id);
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testChildViewId() {
        Id rootId = DefaultPhysicalId.ofObject(1L, 0L, new ClassTypeKey("2_3"));
        var mappingId = DefaultPhysicalId.ofObject(4L, 1L, new ClassTypeKey("4_0"));
        ViewId rootViewId = new DefaultViewId(false, mappingId, rootId);

        ChildViewId childId = new ChildViewId(false, mappingId, rootId, rootViewId);

        var parsedId = Id.parse(childId.toString());
        assertEquals(childId, parsedId);
    }

    public void testFieldViewId() {
        var sourceId = DefaultPhysicalId.ofObject(1L, 0L, new ClassTypeKey("2_3"));
        var mappingId = DefaultPhysicalId.ofObject(4L, 1L, new ClassTypeKey("4_0"));
        var parentId = new DefaultViewId(false, mappingId, sourceId);

        var mappingId2 = DefaultPhysicalId.ofObject(5L, 1L, new ClassTypeKey("4_0"));
        var fieldId = DefaultPhysicalId.ofObject(6L, 1L, new ClassTypeKey("4_0"));
        Id id = new FieldViewId(false, parentId, mappingId2, fieldId, null, new AnyTypeKey());
        Assert.assertEquals(id, Id.parse(id.toString()));
    }

    public void testElementViewId() {
        var sourceId = DefaultPhysicalId.ofObject(1L, 0L, new ClassTypeKey("2_3"));
        var mappingId = DefaultPhysicalId.ofObject(4L, 1L, new ClassTypeKey("4_0"));
        var parentId = new DefaultViewId(false, mappingId, sourceId);
        var typeId = new ClassTypeKey("10_0");

        var mappingId2 = DefaultPhysicalId.ofObject(5L, 1L, new ClassTypeKey("4_0"));
        Id id = new ElementViewId(false, parentId, mappingId2, 2, null, typeId);
        var recovered = Id.parse(id.toString());
        Assert.assertEquals(id, recovered);
    }


}