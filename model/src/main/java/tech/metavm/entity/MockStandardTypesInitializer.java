package tech.metavm.entity;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.Instances;

import java.util.List;
import java.util.Set;

public class MockStandardTypesInitializer {

    public static void init() {
        StandardTypes.anyType = new AnyType();
        StandardTypes.neverType = new NeverType();
        StandardTypes.nullType = new PrimitiveType(PrimitiveKind.NULL);
        StandardTypes.timeType = new PrimitiveType(PrimitiveKind.TIME);
        StandardTypes.booleanType = new PrimitiveType(PrimitiveKind.BOOLEAN);
        StandardTypes.longType = new PrimitiveType(PrimitiveKind.LONG);
        StandardTypes.stringType = new PrimitiveType(PrimitiveKind.STRING);
        StandardTypes.voidType = new PrimitiveType(PrimitiveKind.VOID);
        StandardTypes.passwordType = new PrimitiveType(PrimitiveKind.PASSWORD);
        StandardTypes.doubleType = new PrimitiveType(PrimitiveKind.DOUBLE);
        StandardTypes.nullableAnyType = new UnionType(null, Set.of(StandardTypes.nullType, StandardTypes.anyType));
        StandardTypes.anyArrayType = new ArrayType(null, StandardTypes.anyType, ArrayKind.READ_WRITE);
        StandardTypes.nullableStringType = new UnionType(null, Set.of(StandardTypes.nullType, StandardTypes.stringType));

        var enumTypeParam = new TypeVariable(null, "枚举类型", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumType = ClassTypeBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build();
        enumTypeParam.setBounds(List.of(enumType));
        FieldBuilder.newBuilder("名称", "name", enumType, StandardTypes.getStringType()).build();
        FieldBuilder.newBuilder("序号", "ordinal", enumType, StandardTypes.getLongType()).build();
        StandardTypes.enumType = enumType;
        Instances.nullInstance = new NullInstance(StandardTypes.getNullType());
        Instances.trueInstance = new BooleanInstance(true, StandardTypes.getBooleanType());
        Instances.falseInstance = new BooleanInstance(false, StandardTypes.getBooleanType());

        StandardTypes.entityType = ClassTypeBuilder.newBuilder("实体", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build();
    }

}
