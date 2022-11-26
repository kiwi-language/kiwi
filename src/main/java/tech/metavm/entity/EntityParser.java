package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.ModelMap;

import java.util.function.Function;

public class EntityParser<T extends Entity> extends PojoParser<T, EntityDef<T>> {

    public static <T extends Entity> EntityDef<T> parse(Class<T> entityType,
                                                        Function<Object, IInstance> getInstance,
                                                        DefMap defMap,
                                                        ModelMap modelMap) {
        return new EntityParser<>(entityType, getInstance, defMap, modelMap).parse();
    }

    public EntityParser(Class<T> entityType, Function<Object, IInstance> getInstance, DefMap defMap, ModelMap modelMap) {
        super(entityType, getInstance, defMap, modelMap);
    }

    @Override
    protected EntityDef<T> createDef() {
        return new EntityDef<>(
                null,
                entityType,
                defMap.getPojoDef(entityType.getSuperclass()),
                type
        );
    }
}
