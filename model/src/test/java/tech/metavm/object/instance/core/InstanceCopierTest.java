package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;

public class InstanceCopierTest extends TestCase {

    public void test() {
        StandardTypes.nullType = new PrimitiveType(PrimitiveKind.NULL);
        var stringType = StandardTypes.stringType = new PrimitiveType(PrimitiveKind.STRING);

        ClassType barType = ClassTypeBuilder.newBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, stringType).build();

        ClassType bazType = ClassTypeBuilder.newBuilder("Baz", "Baz").build();
        var bazCodeField = FieldBuilder.newBuilder("code", "code", bazType, stringType).build();
        var bazBarField = FieldBuilder.newBuilder("bar", "bar", bazType, barType).build();

        var barBazField = FieldBuilder.newBuilder("baz", "baz", barType, bazType).build();

        var bazArrayType = new ArrayType(null, bazType, ArrayKind.CHILD);

        ClassType fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, stringType).build();
        var fooBarField = FieldBuilder.newBuilder("bar", "bar", fooType, barType)
                .isChild(true).build();
        var fooBazListField = FieldBuilder.newBuilder("bazList", "bazList", fooType, bazArrayType)
                .isChild(true).build();
        var barFooField = FieldBuilder.newBuilder("foo", "foo", barType, fooType).build();

        ClassInstance foo = ClassInstance.allocate(fooType);
        foo.initField(fooNameField, new StringInstance("leen", stringType));
        var bar = ClassInstance.allocate(barType, new InstanceParentRef(foo, fooBarField));
        bar.initField(barCodeField, new StringInstance("bar001", stringType));
        bar.initField(barFooField, foo);

        var bazArray = new ArrayInstance(bazArrayType, new InstanceParentRef(foo, fooBazListField));

        var baz = ClassInstance.allocate(bazType, new InstanceParentRef(bazArray, null));
        baz.initField(bazCodeField, new StringInstance("baz001", stringType));
        baz.initField(bazBarField, bar);

        bar.initField(barBazField, baz);

        var fooCopy = foo.accept(new InstanceCopier(foo));
        System.out.println(fooCopy);
    }

}