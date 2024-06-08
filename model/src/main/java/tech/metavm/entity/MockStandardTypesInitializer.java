package tech.metavm.entity;

import tech.metavm.object.type.ClassSource;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.util.NncUtils;

import java.util.List;

public class MockStandardTypesInitializer {

    public static void init() {
        StandardTypes.setListKlass(
                ClassTypeBuilder.newBuilder("List", List.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setReadWriteListKlass(
                ClassTypeBuilder.newBuilder("ReadWriteList", "ReadWriteList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ReadWriteListElement", "ReadWriteListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setChildListKlass(
                ClassTypeBuilder.newBuilder("ChildList", "ChildList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ChildListElement", "ChildListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setValueListKlass(
                ClassTypeBuilder.newBuilder("ValueList", "ValueList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "ValueListElement", "ValueListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        var enumTypeParam = new TypeVariable(NncUtils.randomNonNegative(), "EnumType", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = ClassTypeBuilder.newBuilder("Enum", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .tmpId(NncUtils.randomNonNegative())
                .build();
        enumTypeParam.setBounds(List.of(enumType.getType()));
        FieldBuilder.newBuilder("name", "name", enumType, StandardTypes.getStringType()).build();
        FieldBuilder.newBuilder("ordinal", "ordinal", enumType, StandardTypes.getLongType()).build();
        StandardTypes.setEnumKlass(enumType);
        StandardTypes.setEntityKlass(ClassTypeBuilder.newBuilder("Entity", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        StandardTypes.setPredicateKlass(
                ClassTypeBuilder.newBuilder("Predicate", "Predicate")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setConsumerKlass(
                ClassTypeBuilder.newBuilder("Consumer", "Consumer")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "Element", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setThrowableKlass(
                ClassTypeBuilder.newBuilder("Throwable", "Throwable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setExceptionKlass(
                ClassTypeBuilder.newBuilder("Exception", "Exception")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setRuntimeExceptionKlass(
                ClassTypeBuilder.newBuilder("RuntimeException", "RuntimeException")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .build()
        );
        StandardTypes.setIterableKlass(
                ClassTypeBuilder.newBuilder("Iterable", "Iterable")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setIteratorKlass(
                ClassTypeBuilder.newBuilder("Iterator", "Iterator")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "T", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
    }

}
