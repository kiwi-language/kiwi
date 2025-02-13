package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MockStandardTypesInitializer {

    public static long nextKlassTag = 1;
    public static int nextTreeId = 1;

    public static void init() {
        if (ModelDefRegistry.isDefContextPresent()) return;
        StdKlass.list.set(
                newKlassBuilder(List.class)
                        .source(ClassSource.BUILTIN)
                        .typeParameters(new TypeVariable(TmpId.random(), "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.arrayList.set(
                newKlassBuilder(ArrayList.class)
                        .source(ClassSource.BUILTIN)
                        .typeParameters(new TypeVariable(TmpId.random(), "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        var enumTypeParam = new TypeVariable(TmpId.random(), "E",
                DummyGenericDeclaration.INSTANCE);
        var enumType = newKlassBuilder(Enum.class)
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
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
                        .typeParameters(new TypeVariable(TmpId.random(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.consumer.set(
                newKlassBuilder(Consumer.class)
                        .source(ClassSource.BUILTIN)
                        .typeParameters(new TypeVariable(TmpId.random(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.throwable.set(
                newKlassBuilder(Throwable.class)
                        .source(ClassSource.BUILTIN)
                        .build()
        );
        StdKlass.exception.set(
                newKlassBuilder(Exception.class)
                        .source(ClassSource.BUILTIN)
                        .build()
        );
        var runtimeExceptionKlass = newKlassBuilder(RuntimeException.class)
                .source(ClassSource.BUILTIN)
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
                        .typeParameters(new TypeVariable(TmpId.random(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StdKlass.iterator.set(
                newKlassBuilder(Iterator.class)
                        .source(ClassSource.BUILTIN)
                        .typeParameters(new TypeVariable(TmpId.random(), "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
    }

    private static KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return KlassBuilder.newBuilder(PhysicalId.of(nextTreeId++, 0), javaClass.getSimpleName(), javaClass.getName()).tag(nextKlassTag++);
    }

}
