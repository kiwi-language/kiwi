package tech.metavm.entity;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.Instances;

import java.util.List;
import java.util.Set;

public class MockStandardTypesInitializer {

    public static void init() {
        StandardTypes.setAnyType(new AnyType());
        StandardTypes.setNeverType(new NeverType());
        StandardTypes.setNullType(new PrimitiveType(PrimitiveKind.NULL));
        StandardTypes.setTimeType(new PrimitiveType(PrimitiveKind.TIME));
        StandardTypes.setBooleanType(new PrimitiveType(PrimitiveKind.BOOLEAN));
        StandardTypes.setLongType(new PrimitiveType(PrimitiveKind.LONG));
        StandardTypes.setStringType(new PrimitiveType(PrimitiveKind.STRING));
        StandardTypes.setVoidType(new PrimitiveType(PrimitiveKind.VOID));
        StandardTypes.setPasswordType(new PrimitiveType(PrimitiveKind.PASSWORD));
        StandardTypes.setDoubleType(new PrimitiveType(PrimitiveKind.DOUBLE));
        StandardTypes.setNullableAnyType(new UnionType(null, Set.of(StandardTypes.getNullType(), StandardTypes.getAnyType())));
        StandardTypes.setAnyArrayType(new ArrayType(null, StandardTypes.getAnyType(), ArrayKind.READ_WRITE));
        StandardTypes.setReadonlyAnyArrayType(new ArrayType(null, StandardTypes.getAnyType(), ArrayKind.READ_ONLY));
        StandardTypes.setNeverArrayType(new ArrayType(null, StandardTypes.getNeverType(), ArrayKind.READ_WRITE));
        StandardTypes.setNullableStringType(new UnionType(null, Set.of(StandardTypes.getNullType(), StandardTypes.getStringType())));

        var enumTypeParam = new TypeVariable(null, "枚举类型", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = ClassTypeBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build();
        enumTypeParam.setBounds(List.of(enumType));
        FieldBuilder.newBuilder("名称", "name", enumType, StandardTypes.getStringType()).build();
        FieldBuilder.newBuilder("序号", "ordinal", enumType, StandardTypes.getLongType()).build();
        StandardTypes.setEnumType(enumType);
        Instances.setNullInstance(new NullInstance(StandardTypes.getNullType()));
        Instances.setTrueInstance(new BooleanInstance(true, StandardTypes.getBooleanType()));
        Instances.setFalseInstance(new BooleanInstance(false, StandardTypes.getBooleanType()));

        StandardTypes.setEntityType(ClassTypeBuilder.newBuilder("实体", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
    }

}
