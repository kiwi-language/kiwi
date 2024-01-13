package tech.metavm.util;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.type.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUtils {

    public static FooType createFooType(boolean initIds) {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, StandardTypes.getStringType())
                .asTitle().build();
        var barType = ClassTypeBuilder.newBuilder("Bar", "Bar").build();
        var barCodeField = FieldBuilder.newBuilder("code", "code", barType, StandardTypes.getStringType())
                .asTitle().build();
        var barArrayType = new ArrayType(null, barType, ArrayKind.CHILD);
        var nullableBarType = new UnionType(null, Set.of(barType, StandardTypes.getNullType()));
        var fooBarsField = FieldBuilder.newBuilder("bars", "bars", fooType, barArrayType)
                .isChild(true).build();
        var bazType = ClassTypeBuilder.newBuilder("Baz", "Baz").build();
        var bazBarField = FieldBuilder.newBuilder("bar", "bar", bazType, nullableBarType).build();
        if(initIds) {
            fooType.initId(1001L);
            barType.initId(1002L);
            bazType.initId(1003L);
            barArrayType.initId(1008L);
            nullableBarType.initId(1009L);
            fooNameField.initId(2001L);
            fooBarsField.initId(2002L);
            barCodeField.initId(2003L);
            bazBarField.initId(2004L);
        }
        return new FooType(fooType, barType, bazType, barArrayType, fooNameField, fooBarsField, barCodeField, bazBarField);
    }

    public static ClassInstance createFoo(FooType fooType) {
        return ClassInstanceBuilder.newBuilder(fooType.fooType())
                .data(Map.of(
                        fooType.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooType.fooBarsField(),
                        new ArrayInstance(
                                fooType.barChildArrayType(),
                                List.of(
                                        ClassInstanceBuilder.newBuilder(fooType.barType())
                                                .data(Map.of(
                                                        fooType.barCodeField(),
                                                        Instances.stringInstance("bar001")
                                                ))
                                                .build(),
                                        ClassInstanceBuilder.newBuilder(fooType.barType())
                                                .data(Map.of(
                                                        fooType.barCodeField(),
                                                        Instances.stringInstance("bar002")
                                                ))
                                                .build()
                                )
                        )
                ))
                .build();
    }


}
