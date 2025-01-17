package org.metavm.entity;

import org.metavm.api.ChildList;
import org.metavm.api.ValueList;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
import org.metavm.object.type.*;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MockStandardTypesInitializer {

    public static long nextKlassTag = 1;

    public static void init() {
        if (ModelDefRegistry.isDefContextPresent()) return;
        StdKlass.list.set(
                newKlassBuilder(List.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "Element", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.arrayList.set(
                newKlassBuilder(ArrayList.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "ReadWriteListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.childList.set(
                newKlassBuilder(ChildList.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "ChildListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.valueList.set(
                newKlassBuilder(ValueList.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "ValueListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        var enumTypeParam = new TypeVariable(Utils.randomNonNegative(), "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = newKlassBuilder(Enum.class)
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .tmpId(Utils.randomNonNegative())
                .build();
        enumTypeParam.setBounds(List.of(enumType.getType()));
        FieldBuilder.newBuilder("name", enumType, Types.getStringType()).build();
        FieldBuilder.newBuilder("ordinal", enumType, Types.getIntType()).build();
        StdKlass.enum_.set(enumType);
        StdKlass.entity.set(newKlassBuilder(Entity.class)
                .source(ClassSource.BUILTIN)
                .build());
        StdKlass.predicate.set(
                newKlassBuilder(Predicate.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "Element", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.consumer.set(
                newKlassBuilder(Consumer.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "Element", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.throwable.set(
                newKlassBuilder(Throwable.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .build()
        );
        StdKlass.exception.set(
                newKlassBuilder(Exception.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .build()
        );
        var runtimeExceptionKlass = newKlassBuilder(RuntimeException.class)
                .source(ClassSource.BUILTIN)
                .tmpId(Utils.randomNonNegative())
                .build();
        {
            var constructor = MethodBuilder.newBuilder(runtimeExceptionKlass, runtimeExceptionKlass.getName())
                    .isConstructor(true)
                    .parameters(new NameAndType("message", Types.getNullableStringType()))
                    .build();
            var code = constructor.getCode();
            Nodes.this_(code);
            Nodes.ret(code);
        }
        StdKlass.runtimeException.set(runtimeExceptionKlass);
        StdKlass.iterable.set(
                newKlassBuilder(Iterable.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.iterator.set(
                newKlassBuilder(Iterator.class)
                        .source(ClassSource.BUILTIN)
                        .tmpId(Utils.randomNonNegative())
                        .typeParameters(new TypeVariable(Utils.randomNonNegative(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
    }

    private static KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return KlassBuilder.newBuilder(javaClass.getSimpleName(), javaClass.getName()).tag(nextKlassTag++);
    }

}
