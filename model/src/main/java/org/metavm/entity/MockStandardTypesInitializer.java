package org.metavm.entity;

import org.metavm.object.type.ClassSource;
import org.metavm.object.type.KlassBuilder;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.NncUtils;

import java.util.List;

public class MockStandardTypesInitializer {

    public static void init() {
        StandardTypes.setListKlass(
                KlassBuilder.newBuilder("List", List.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setReadWriteListKlass(
                KlassBuilder.newBuilder("ReadWriteList", "ReadWriteList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ReadWriteListElement", "ReadWriteListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setChildListKlass(
                KlassBuilder.newBuilder("ChildList", "ChildList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ChildListElement", "ChildListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setValueListKlass(
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
        FieldBuilder.newBuilder("name", "name", enumType, StandardTypes.getStringType()).build();
        FieldBuilder.newBuilder("ordinal", "ordinal", enumType, StandardTypes.getLongType()).build();
        StandardTypes.setEnumKlass(enumType);
        StandardTypes.setEntityKlass(KlassBuilder.newBuilder("Entity", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        StandardTypes.setPredicateKlass(
                KlassBuilder.newBuilder("Predicate", "Predicate")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setConsumerKlass(
                KlassBuilder.newBuilder("Consumer", "Consumer")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setThrowableKlass(
                KlassBuilder.newBuilder("Throwable", "Throwable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setExceptionKlass(
                KlassBuilder.newBuilder("Exception", "Exception")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setRuntimeExceptionKlass(
                KlassBuilder.newBuilder("RuntimeException", "RuntimeException")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setIterableKlass(
                KlassBuilder.newBuilder("Iterable", "Iterable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setIteratorKlass(
                KlassBuilder.newBuilder("Iterator", "Iterator")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
    }

}
