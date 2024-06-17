package org.metavm.entity;

import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import java.util.List;

public class MockStandardTypesInitializer {

    public static void init() {
        BuiltinKlasses.list.set(
                KlassBuilder.newBuilder("List", List.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.arrayList.set(
                KlassBuilder.newBuilder("ReadWriteList", "ReadWriteList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ReadWriteListElement", "ReadWriteListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.childList.set(
                KlassBuilder.newBuilder("ChildList", "ChildList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ChildListElement", "ChildListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.valueList.set(
                KlassBuilder.newBuilder("ValueList", "ValueList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ValueListElement", "ValueListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        var enumTypeParam = new TypeVariable(NncUtils.randomNonNegative(), "EnumType", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = KlassBuilder.newBuilder("Enum", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .tmpId(NncUtils.randomNonNegative())
                .build();
        enumTypeParam.setBounds(List.of(enumType.getType()));
        FieldBuilder.newBuilder("name", "name", enumType, Types.getStringType()).build();
        FieldBuilder.newBuilder("ordinal", "ordinal", enumType, Types.getLongType()).build();
        BuiltinKlasses.enum_.set(enumType);
        BuiltinKlasses.entity.set(KlassBuilder.newBuilder("Entity", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        BuiltinKlasses.predicate.set(
                KlassBuilder.newBuilder("Predicate", "Predicate")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.consumer.set(
                KlassBuilder.newBuilder("Consumer", "Consumer")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.throwable.set(
                KlassBuilder.newBuilder("Throwable", "Throwable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        BuiltinKlasses.exception.set(
                KlassBuilder.newBuilder("Exception", "Exception")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        BuiltinKlasses.runtimeException.set(
                KlassBuilder.newBuilder("RuntimeException", "RuntimeException")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        BuiltinKlasses.iterable.set(
                KlassBuilder.newBuilder("Iterable", "Iterable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        BuiltinKlasses.iterator.set(
                KlassBuilder.newBuilder("Iterator", "Iterator")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
    }

}
