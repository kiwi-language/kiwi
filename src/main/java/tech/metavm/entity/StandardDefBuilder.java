package tech.metavm.entity;

import tech.metavm.object.meta.IdConstants;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.Date;

public class StandardDefBuilder {

    private ValueDef<Object> objectDef;

    private ArrayDef<Object> arrayDef;

    private ValueDef<Enum<?>> enumDef;

    public void initRootTypes(DefMap defMap) {

        defMap.addDef(new PrimitiveDef<>(
                Integer.class,
                StandardTypes.INT
        ));

        defMap.addDef(new PrimitiveDef<>(
                Long.class,
                StandardTypes.LONG
        ));

        defMap.addDef(new PrimitiveDef<>(
                Double.class,
                StandardTypes.DOUBLE
        ));

        defMap.addDef(new PrimitiveDef<>(
                Boolean.class,
                StandardTypes.BOOL
        ));

        defMap.addDef(new PrimitiveDef<>(
                String.class,
                StandardTypes.STRING
        ));

        defMap.addDef(new PrimitiveDef<>(
                Date.class,
                StandardTypes.TIME
        ));

        defMap.addDef(new PrimitiveDef<>(
                Password.class,
                StandardTypes.PASSWORD
        ));


        defMap.addDef(new PrimitiveDef<>(
                Null.class,
                StandardTypes.NULL
        ));

        objectDef = new ValueDef<>(
                "对象",
                Object.class,
                null,
                StandardTypes.OBJECT,
                defMap
        );

        defMap.addDef(objectDef);

        ValueDef<Record> recordDef = new ValueDef<>(
                "记录",
                Record.class,
                null,
                StandardTypes.RECORD,
                defMap
        );
        defMap.addDef(recordDef);

        EntityDef<Entity> entityDef = new EntityDef<>(
                "实体",
                Entity.class,
                objectDef,
                StandardTypes.ENTITY,
                defMap
        );

        defMap.addDef(entityDef);

        arrayDef = new ArrayDef<>(
                objectDef,
                Table.class,
                IdConstants.ARRAY
        );

        defMap.addDef(arrayDef);

        enumDef = new ValueDef<>(
                "枚举",
                new TypeReference<>() {},
                objectDef,
                StandardTypes.ENUM,
                defMap
        );

        createFieldDef(
                ReflectUtils.getField(Enum.class, "name"),
                StandardTypes.ENUM_NAME,
                enumDef
        );

        createFieldDef(
                ReflectUtils.getField(Enum.class, "ordinal"),
                StandardTypes.ENUM_ORDINAL,
                enumDef
        );

        defMap.addDef(enumDef);
    }

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
                                PojoDef<?> declaringTypeDef
                                ) {
        new FieldDef(
                field,
                reflectField,
                declaringTypeDef,
                null
        );
    }
    
}
