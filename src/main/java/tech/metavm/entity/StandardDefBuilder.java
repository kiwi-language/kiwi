package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.*;

import static tech.metavm.util.ReflectUtils.*;

public class StandardDefBuilder {

    private ObjectTypeDef<Object> objectDef;

    private ValueDef<Enum<?>> enumDef;

    private PrimitiveDef<String> stringDef;

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

        ObjectType objectType = new ObjectType();

        objectDef = new ObjectTypeDef<>(
                Object.class,
                objectType
        );
        defMap.addDef(objectDef);

        var nullType = typeFactory.createPrimitive(PrimitiveKind.NULL);
        var longType = typeFactory.createPrimitive( PrimitiveKind.LONG);
        var doubleType = typeFactory.createPrimitive( PrimitiveKind.DOUBLE);
        var stringType = typeFactory.createPrimitive(PrimitiveKind.STRING);
        var boolType = typeFactory.createPrimitive( PrimitiveKind.BOOLEAN);
        var timeType = typeFactory.createPrimitive(PrimitiveKind.TIME);
        var passwordType = typeFactory.createPrimitive(PrimitiveKind.PASSWORD);
        var voidType = typeFactory.createPrimitive(PrimitiveKind.VOID);

        Map<Class<?>, PrimitiveType> primitiveTypes = Map.of(
            Null.class, nullType,
                Long.class, longType,
                Double.class, doubleType,
                String.class, stringType,
                Boolean.class, boolType,
                Date.class, timeType,
                Password.class, passwordType,
                Void.class, voidType
        );
        var primTypeFactory = new TypeFactory(primitiveTypes::get);
        var primTypeSource = new MapTypeSource(primitiveTypes.values());
        var primDefMap = new HashMap<PrimitiveType, PrimitiveDef<?>>();
        primDefMap.put(nullType, nullDef = new PrimitiveDef<>(Null.class, nullType));
        primDefMap.put(longType, longDef = new PrimitiveDef<>(Long.class, longType));
        primDefMap.put(doubleType, new PrimitiveDef<>(Double.class, doubleType));
        primDefMap.put(boolType, new PrimitiveDef<>(Boolean.class, boolType));
        primDefMap.put(stringType, stringDef = new PrimitiveDef<>(String.class, stringType));
        primDefMap.put(timeType, new PrimitiveDef<>(Date.class, timeType));
        primDefMap.put(passwordType, new PrimitiveDef<>(Password.class, passwordType));
        primDefMap.put(voidType, new PrimitiveDef<>(Void.class, voidType));

        for (PrimitiveType primitiveType : primitiveTypes.values()) {
            if(!primitiveType.isNull()) {
                var def = primDefMap.get(primitiveType);
                TypeUtil.fillCompositeTypes(primitiveType, primTypeFactory);
                def.addCollectionType(TypeUtil.getIteratorType(primitiveType, primTypeSource, primTypeFactory));
                def.addCollectionType(TypeUtil.getCollectionType(primitiveType, primTypeSource, primTypeFactory));
                def.addCollectionType(TypeUtil.getListType(primitiveType, primTypeSource, primTypeFactory));
                def.addCollectionType(TypeUtil.getSetType(primitiveType, primTypeSource, primTypeFactory));
                def.addCollectionType(TypeUtil.getIteratorImplType(primitiveType, primTypeSource, primTypeFactory));
            }
        }
        primDefMap.values().forEach(defMap::addDef);

//        defMap.addDef(nullDef = new PrimitiveDef<>(Null.class, nullType));
//        defMap.addDef(longDef = new PrimitiveDef<>(Long.class, longType));
//        defMap.addDef(new PrimitiveDef<>(Double.class, doubleType));
//        defMap.addDef(new PrimitiveDef<>(Boolean.class, boolType));
//        defMap.addDef(stringDef = new PrimitiveDef<>(String.class, stringType));
//        defMap.addDef(new PrimitiveDef<>(Date.class, timeType));
//        defMap.addDef(new PrimitiveDef<>(Password.class, passwordType));
//        defMap.addDef(new PrimitiveDef<>(Void.class, voidType));

        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                ClassBuilder.newBuilder("记录", Record.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .category(TypeCategory.VALUE).build(),
                defMap
        );
        defMap.addDef(recordDef);

        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                ClassBuilder.newBuilder( "实体", Entity.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .build(),
                defMap
        );

        defMap.addDef(entityDef);

        objectType.setArrayType(typeFactory.createArrayType(objectType));
        defMap.addDef(
                CollectionDef.createHelper(
                    Table.class,
                    Table.class,
                    objectDef,
                    objectType.getArrayType()
                )
        );

        ClassType enumType = ClassBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.REFLECTION)
                .build();
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
                createField(ENUM_ORDINAL_FIELD, false, longType, enumType),
                enumDef
        );

        defMap.addDef(enumDef);

        defMap.addDef(new InstanceDef<>(Instance.class, objectType));
        defMap.addDef(new InstanceDef<>(ClassInstance.class, objectType));
        defMap.addDef(new InstanceDef<>(ArrayInstance.class, objectType));
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
                javaField.getName(),
                declaringType,
                type, Access.GLOBAL,
                false,
                asTitle,
                new NullInstance((PrimitiveType) nullDef.getType()),
                false,
                false,
                new NullInstance((PrimitiveType) nullDef.getType())
        );
    }

    public ObjectTypeDef<Object> getObjectDef() {
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
    public ObjectType getObjectType() {
        return objectDef.getType();
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
