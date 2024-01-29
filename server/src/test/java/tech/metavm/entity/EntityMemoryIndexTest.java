package tech.metavm.entity;

import junit.framework.TestCase;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.view.FieldsObjectMapping;

import java.util.List;

public class EntityMemoryIndexTest extends TestCase {

    public void test() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var fooViewType = ClassTypeBuilder.newBuilder("FooView", "FooView").ephemeral(true).build();
        var fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);
        var fooViewArrayType = new ArrayType(null, fooViewType, ArrayKind.CHILD);
        var fooMapping = new FieldsObjectMapping(
                null, "FooBuiltinMapping", "FooBuiltinMapping", fooType,
                true, fooViewType, List.of()
        );
    }

}