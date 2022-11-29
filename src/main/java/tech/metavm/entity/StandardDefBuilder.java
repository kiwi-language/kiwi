package tech.metavm.entity;

import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Field;

public class StandardDefBuilder {

    private ValueDef<Object> objectDef;

    private ArrayDef<Object> arrayDef;

    private ValueDef<Enum<?>> enumDef;

    public void initRootTypes(DefMap defMap) {
//        Instance objectTypeInst = getInstance.apply(Object.class);
        objectDef = new ValueDef<>(
                "对象",
                Object.class,
                null,
                StandardTypes.OBJECT,
                defMap
        );

        defMap.putDef(Object.class, objectDef);

//        Instance recordTypeInst = getInstance.apply(Record.class);
        ValueDef<Record> recordDef = new ValueDef<>(
                "记录",
                Record.class,
                null,
                StandardTypes.RECORD,
                defMap
        );
        defMap.putDef(Record.class, recordDef);

//        Instance entityTypeInst = getInstance.apply(Entity.class);
        EntityDef<Entity> entityDef = new EntityDef<>(
                "实体",
                Entity.class,
                objectDef,
                StandardTypes.ENTITY,
                defMap
        );

        defMap.putDef(Entity.class, entityDef);

//        Instance tableTypeInst = getInstance.apply(Table.class);
        arrayDef = new ArrayDef<>(
                objectDef,
                Table.class,
                StandardTypes.ARRAY
        );

        defMap.putDef(Table.class, arrayDef);

//        Instance enumTypeInst = getInstance.apply(Enum.class);
        enumDef = new ValueDef<>(
                "枚举",
                new TypeReference<>() {},
                objectDef,
                StandardTypes.ENUM,
                defMap
        );

//        defMap.putDef(Enum.class, enumDef);
//
//        putPrimitiveType(Integer.class, StandardTypes.INT, defMap);
//
//        putPrimitiveType(Long.class, StandardTypes.LONG, defMap);
//
//        putPrimitiveType(Double.class, StandardTypes.DOUBLE, defMap);
//
//        putPrimitiveType(Boolean.class, StandardTypes.BOOL, defMap);
//
//        putPrimitiveType(String.class, StandardTypes.STRING, defMap);
//
//        putPrimitiveType(Date.class, StandardTypes.TIME, defMap);
//
//        putPrimitiveType(Password.class, StandardTypes.PASSWORD, defMap);
//
//        putPrimitiveType(Null.class, StandardTypes.NULL, defMap);

        createFieldDef(
                ReflectUtils.getField(Enum.class, "name"),
                StandardTypes.ENUM_NAME,
                "名称",
                enumDef
        );

        createFieldDef(
                ReflectUtils.getField(Enum.class, "ordinal"),
                StandardTypes.ENUM_ORDINAL,
                "序号",
                enumDef
        );
    }

//    public void putPrimitiveType(Class<?> entityType, tech.metavm.object.meta.Type primitiveType, DefMap defMap) {
//        defMap.putDef(
//                entityType,
//                new ValueDef<>(
//                        primitiveType.getName(),
//                        entityType,
//                        objectDef,
//                        primitiveType,
//                        defMap
//                )
//        );
//    }

    public ValueDef<Object> getObjectDef() {
        return objectDef;
    }

    public ArrayDef<Object> getArrayDef() {
        return arrayDef;
    }

    public ValueDef<Enum<?>> getEnumDef() {
        return enumDef;
    }

    private void createFieldDef(Field reflectField,
                                tech.metavm.object.meta.Field field,
                                String name,
                                PojoDef<?> declaringTypeDef
                                ) {
        new FieldDef(
                name,
                field,
                reflectField,
                declaringTypeDef,
                null
        );
    }
    
}
