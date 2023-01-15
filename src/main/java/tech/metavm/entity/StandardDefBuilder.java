package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.Null;
import tech.metavm.util.Password;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Field;
import java.util.Date;

import static tech.metavm.object.meta.PrimitiveKind.INT;
import static tech.metavm.util.ReflectUtils.*;

public class StandardDefBuilder {

    private AnyTypeDef<Object> objectDef;

//    private CollectionDef<?, Table<?>> tableDef;

    private ValueDef<Enum<?>> enumDef;

    private PrimitiveDef<String> stringDef;

    private PrimitiveDef<Integer> intDef;

    private PrimitiveDef<Long> longDef;

    private PrimitiveDef<Null> nullDef;

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
        objectType.setArrayType(new ArrayType(objectType));

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

        nullDef = new PrimitiveDef<>(
                Null.class,
                typeFactory.createPrimitive(PrimitiveKind.NULL)
        );
        defMap.addDef(nullDef);

        defMap.addDef(objectDef);

        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                typeFactory.createValueClass("记录", Record.class.getSimpleName(), null),
                defMap
        );
        defMap.addDef(recordDef);

        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                typeFactory.createClass("实体", Entity.class.getSimpleName(),null),
                defMap
        );

        defMap.addDef(entityDef);

        defMap.addDef(
                CollectionDef.createHelper(
                    Table.class,
                    Table.class,
                    objectDef,
                    objectType.getArrayType()
                )
        );

        ClassType enumType = typeFactory.createClass("枚举", Enum.class.getSimpleName(), null);
        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {}.getType(),
                enumType,
                defMap
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, stringType, enumType),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, intType, enumType),
                enumDef
        );

        defMap.addDef(enumDef);

        defMap.addDef(new InstanceDef<>(Instance.class));
        defMap.addDef(new InstanceDef<>(ClassInstance.class));
        defMap.addDef(new InstanceDef<>(ArrayInstance.class));
    }


    @SuppressWarnings("SameParameterValue")
    private <T extends Entity> EntityDef<T> createEntityDef(java.lang.reflect.Type javaType,
                                                            Class<T> javaClass,
                                                            ClassType type,
                                                            DefMap defMap) {
        return new EntityDef<>(
                javaClass,
                javaType,
                null,
                type,
                defMap
        );
    }

    @SuppressWarnings("SameParameterValue")
    private <T> ValueDef<T> createValueDef(java.lang.reflect.Type javaType,
                                           Class<T> javaClass,
                                           ClassType type,
                                           DefMap defMap) {
        return new ValueDef<>(
                javaClass,
                javaType,
                null,
                type,
                defMap
        );
    }

    private tech.metavm.object.meta.Field createField(Field javaField,
                                                      boolean asTitle,
                                                      Type type,
                                                      ClassType declaringType) {
        return new tech.metavm.object.meta.Field(
                getMetaFieldName(javaField),
                declaringType,
                Access.GLOBAL,
                false,
                asTitle,
                new NullInstance((PrimitiveType) nullDef.getType()),
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
