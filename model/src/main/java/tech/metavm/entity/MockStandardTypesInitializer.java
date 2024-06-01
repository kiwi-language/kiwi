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
                ClassTypeBuilder.newBuilder("列表", List.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "元素类型", "E", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setReadWriteListKlass(
                ClassTypeBuilder.newBuilder("读写列表", "ReadWriteList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "读写列表元素", "ReadWriteListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setChildListKlass(
                ClassTypeBuilder.newBuilder("子对象列表", "ChildList")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "子对象列表元素", "ChildListElement", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        var enumTypeParam = new TypeVariable(NncUtils.randomNonNegative(), "枚举类型", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = ClassTypeBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .tmpId(NncUtils.randomNonNegative())
                .build();
        enumTypeParam.setBounds(List.of(enumType.getType()));
        FieldBuilder.newBuilder("名称", "name", enumType, StandardTypes.getStringType()).build();
        FieldBuilder.newBuilder("序号", "ordinal", enumType, StandardTypes.getLongType()).build();
        StandardTypes.setEnumKlass(enumType);
        StandardTypes.setEntityKlass(ClassTypeBuilder.newBuilder("实体", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        StandardTypes.setPredicateKlass(
                ClassTypeBuilder.newBuilder("断言", "Predicate")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "元素", "T", DummyGenericDeclaration.INSTANCE))
                        .build()
        );
        StandardTypes.setConsumerKlass(
                ClassTypeBuilder.newBuilder("消费者", "Consumer")
                        .source(ClassSource.BUILTIN)
                        .tmpId(NncUtils.randomNonNegative())
                        .typeParameters(new TypeVariable(NncUtils.randomNonNegative(), "元素", "T", DummyGenericDeclaration.INSTANCE))
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
