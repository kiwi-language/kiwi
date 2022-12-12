package tech.metavm.entity;

import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.Date;

import static tech.metavm.object.meta.PrimitiveKind.INT;

public class StandardDefBuilder {

    private AnyTypeDef<Object> objectDef;

//    private CollectionDef<?, Table<?>> tableDef;

    private ValueDef<Enum<?>> enumDef;

    private PrimitiveDef<String> stringDef;

    private PrimitiveDef<Integer> intDef;

    private PrimitiveDef<Long> longDef;

    private FieldDef enumNameDef;

    private FieldDef enumOrdinalDef;

    public StandardDefBuilder() {
    }

    public StandardDefBuilder(DefMap defMa) {
        initRootTypes(defMa);
    }

    public void initRootTypes(DefMap defMap) {
        TypeFactory typeFactory = new TypeFactory(defMap::getType);

        AnyType objectType = new AnyType();

        objectDef = new AnyTypeDef<>(
                Object.class,
                objectType
        );

        PrimitiveType intType = typeFactory.createPrimitive(INT);
        defMap.addDef(intDef = new PrimitiveDef<>(
                Integer.class,
                intType
        ));

        defMap.addDef(longDef = new PrimitiveDef<>(
                Long.class,
                typeFactory.createPrimitive( PrimitiveKind.LONG)
        ));

        defMap.addDef(new PrimitiveDef<>(
                Double.class,
                typeFactory.createPrimitive( PrimitiveKind.DOUBLE)
        ));

        defMap.addDef(new PrimitiveDef<>(
                Boolean.class,
                typeFactory.createPrimitive( PrimitiveKind.BOOLEAN)
        ));

        PrimitiveType stringType = typeFactory.createPrimitive(PrimitiveKind.STRING);
        defMap.addDef(stringDef = new PrimitiveDef<>(
                String.class,
                stringType
        ));

        defMap.addDef(new PrimitiveDef<>(
                Date.class,
                typeFactory.createPrimitive(PrimitiveKind.TIME)
        ));

        defMap.addDef(new PrimitiveDef<>(
                Password.class,
                typeFactory.createPrimitive(PrimitiveKind.PASSWORD)
        ));

        defMap.addDef(new PrimitiveDef<>(
                Null.class,
                typeFactory.createPrimitive(PrimitiveKind.NULL)
        ));

        defMap.addDef(objectDef);

        ValueDef<Record> recordDef = new ValueDef<>(
                Record.class,
                Record.class,
                null,
                typeFactory.createValueClass("记录", null),
                defMap
        );
        defMap.addDef(recordDef);

        EntityDef<Entity> entityDef = new EntityDef<>(
                Entity.class,
                Entity.class,
                null,
                typeFactory.createRefClass("实体", null),
                defMap
        );

        defMap.addDef(entityDef);

        defMap.addDef(
                CollectionDef.createHelper(
                    Table.class,
                    Table.class,
                    objectDef,
                    typeFactory.createArrayType(objectType)
                )
        );

        ClassType enumType = typeFactory.createRefClass("枚举", null);
        enumDef = new ValueDef<>(
                new TypeReference<Enum<?>>() {}.getType(),
                Enum.class, // Enum is not a RuntimeGeneric, use the raw class
                null,
                enumType,
                defMap
        );

        enumNameDef = createFieldDef(
                ReflectUtils.getField(Enum.class, "name"),
                createField("名称", true, stringType, enumType),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ReflectUtils.getField(Enum.class, "ordinal"),
                createField("序号", false, intType, enumType),
                enumDef
        );

        defMap.addDef(enumDef);
    }

    private tech.metavm.object.meta.Field createField(String name,
                                                      boolean asTitle,
                                                      Type type,
                                                      ClassType declaringType) {
        return new tech.metavm.object.meta.Field(
                name,
                declaringType,
                Access.GLOBAL,
                false,
                asTitle,
                null,
                type,
                false
        );
    }

    public AnyTypeDef<Object> getObjectDef() {
        return objectDef;
    }

    public ValueDef<Enum<?>> getEnumDef() {
        return enumDef;
    }

    private FieldDef createFieldDef(Field reflectField,
                                tech.metavm.object.meta.Field field,
                                PojoDef<?> declaringTypeDef
                                ) {
        return new FieldDef(
                field,
                false,
                reflectField,
                declaringTypeDef,
                null
        );
    }

    public PrimitiveType getStringType() {
        return (PrimitiveType) stringDef.getType();
    }

    @SuppressWarnings("unused")
    public AnyType getObjectType() {
        return objectDef.getType();
    }

    @SuppressWarnings("unused")
    public PrimitiveType getIntType() {
        return (PrimitiveType) intDef.getType();
    }

    @SuppressWarnings("unused")
    public PrimitiveDef<Long> getLongDef() {
        return longDef;
    }

    public ClassType getEnumType() {
        return enumDef.getType();
    }

    public tech.metavm.object.meta.Field getEnumNameField() {
        return enumNameDef.getField();
    }

    public tech.metavm.object.meta.Field getEnumOrdinalField() {
        return enumOrdinalDef.getField();
    }

}
