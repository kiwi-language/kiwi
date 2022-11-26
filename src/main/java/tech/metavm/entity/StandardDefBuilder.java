package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StandardDefBuilder {

    private ValueDef<Object> objectDef;

    private ArrayDef<Object> arrayDef;

    private ValueDef<Enum<?>> enumDef;

    public void initRootTypes(Function<Object, IInstance> getInstance, BiConsumer<Type, ModelDef<?,?>> putDef, ModelMap modelMap) {
        IInstance objectTypeInst = getInstance.apply(Object.class);
        objectDef = new ValueDef<>(
                "对象",
                Object.class,
                null,
                NncUtils.get(objectTypeInst, modelMap::getType)
        );

        putDef.accept(Object.class, objectDef);

        IInstance recordTypeInst = getInstance.apply(Record.class);
        ValueDef<Record> recordDef = new ValueDef<>(
                "记录",
                Record.class,
                null,
                NncUtils.get(recordTypeInst, modelMap::getType)
        );
        putDef.accept(Record.class, recordDef);

        IInstance entityTypeInst = getInstance.apply(Entity.class);
        EntityDef<Entity> entityDef = new EntityDef<>(
                "实体",
                Entity.class,
                objectDef,
                NncUtils.get(entityTypeInst, modelMap::getType)
        );

        putDef.accept(Entity.class, entityDef);

        IInstance tableTypeInst = getInstance.apply(Table.class);
        arrayDef = new ArrayDef<>(
                objectDef,
                objectDef,
                NncUtils.get(tableTypeInst, modelMap::getType)
        );

        putDef.accept(Table.class, arrayDef);

        IInstance enumTypeInst = getInstance.apply(Enum.class);
        enumDef = new ValueDef<>(
                "枚举",
                new TypeReference<>() {},
                objectDef,
                NncUtils.get(enumTypeInst, modelMap::getType)
        );

        putDef.accept(Enum.class, enumDef);

        createFieldDef(
                ReflectUtils.getField(Enum.class, "name"),
                "名称",
                enumDef,
                getInstance,
                modelMap
        );

        createFieldDef(
                ReflectUtils.getField(Enum.class, "ordinal"),
                "序号",
                enumDef,
                getInstance,
                modelMap
        );
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

    private void createFieldDef(Field field,
                                String name,
                                PojoDef<?> declaringTypeDef,
                                Function<Object, IInstance> getInstance,
                                ModelMap modelMap
                                ) {

        IInstance fieldInst = getInstance.apply(field);
        new FieldDef(
                name,
                NncUtils.get(fieldInst, modelMap::getField),
                field,
                declaringTypeDef,
                null
        );
    }
    
}
