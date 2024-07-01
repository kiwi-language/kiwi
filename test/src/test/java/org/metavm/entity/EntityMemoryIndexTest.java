package org.metavm.entity;

import junit.framework.TestCase;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.view.FieldsObjectMapping;
import org.metavm.util.TestUtils;

import java.util.List;

public class EntityMemoryIndexTest extends TestCase {

    public void test() {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var fooViewType = TestUtils.newKlassBuilder("FooView", "FooView").ephemeral(true).build();
        var fooArrayType = new ArrayType(fooType.getType(), ArrayKind.READ_WRITE);
        var fooViewArrayType = new ArrayType(fooViewType.getType(), ArrayKind.CHILD);
        var fooMapping = new FieldsObjectMapping(
                null, "FooBuiltinMapping", "FooBuiltinMapping", fooType,
                true, fooViewType.getType(), List.of()
        );
    }

}